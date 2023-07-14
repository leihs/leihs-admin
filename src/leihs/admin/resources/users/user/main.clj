(ns leihs.admin.resources.users.user.main
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.set :refer [rename-keys]]
    [compojure.core :as cpj]
    [leihs.admin.common.users-and-groups.core :as users-and-groups]
    [leihs.admin.paths :refer [path]]
    [leihs.admin.resources.users.choose-core :as choose-core]
    [leihs.admin.resources.users.user.core :refer [sql-merge-unique-user]]
    [leihs.core.auth.core :as auth]
    [leihs.core.sql :as sql]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [slingshot.slingshot :refer [try+]]
    [taoensso.timbre :refer [error warn info debug spy]])
  (:import
    [java.awt.image BufferedImage]
    [java.io ByteArrayInputStream ByteArrayOutputStream]
    [java.util Base64]
    [javax.imageio ImageIO]))

;;; data keys ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def user-selects
  [:account_enabled
   :account_disabled_at
   :address
   :badge_id
   :city
   :country
   :created_at
   :email
   :extended_info
   :firstname
   :id
   :img256_url
   :img32_url
   :img_digest
   :is_admin
   :is_system_admin
   :last_sign_in_at
   :lastname
   :login
   :organization
   :org_id
   :password_sign_in_enabled
   :phone
   :admin_protected
   :system_admin_protected
   :secondary_email
   :updated_at
   :url
   :zip
   [(-> (sql/select :%count.*)
        (sql/from :contracts)
        (sql/merge-where [:= :contracts.user_id :users.id]))
    :contracts_count]
   [(-> (sql/select :%count.*)
        (sql/from :access_rights)
        (sql/merge-where [:= :access_rights.user_id :users.id]))
    :inventory_pool_roles_count]])


(def user-write-keys
  [:address
   :account_enabled
   :badge_id
   :city
   :country
   :email
   :extended_info
   :firstname
   :img256_url
   :img32_url
   :img_digest
   :is_admin
   :is_system_admin
   :lastname
   :admin_protected
   :login
   :organization
   :org_id
   :password_sign_in_enabled
   :phone
   :secondary_email
   :system_admin_protected
   :url
   :zip])

(def user-write-keymap
  {})


(def admin-restricted-attributes
  [:admin_protected
   :is_admin
   :org_id
   :organization])

(def system-admin-restricted-attributes
  [:is_system_admin
   :system_admin_protected])



;;; helpers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn requester-is-admin? [request]
  (auth/admin-scopes? request))

(defn requester-is-system-admin? [request]
  (auth/system-admin-scopes? request))

;;; user ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn user-query [uid]
  (-> (apply sql/select user-selects)
      (sql/from :users)
      (sql-merge-unique-user uid)
      (sql/merge-where [:= :delegator_user_id nil])))

(defn get-user [{tx :tx {uid :user-id} :route-params}]
  {:body
   (or (->> (-> uid user-query sql/format)
            (jdbc/query tx) first)
       (throw (ex-info "User not found" {:status 404})))})

;;; delete user ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn throw-forbidden! []
  (throw (ex-info "Forbidden" {:status 403})))

(defn protect-admin-and-system-admin! [user request]
  (cond
    (requester-is-system-admin?
      request)  "OK"
    (requester-is-admin?
      request) (cond (:system_admin_protected
                       user) (throw-forbidden!)
                     (:is_system_admin
                       user) (throw-forbidden!)
                     :else "OK")
    :else (cond (:admin_protected
                  user) (throw-forbidden!)
                (:is_admin
                  user) (throw-forbidden!)
                :else "OK")))


(defn assert-deletion-permitted! [request user]
  (cond (requester-is-system-admin? request) :OK
        (requester-is-admin?
          request) (cond (:is_system_admin user) (throw-forbidden!)
                         (:system_admin_protected user) (throw-forbidden!)
                         :else :OK)
        :else (cond (:is_admin user) (throw-forbidden!)
                    (:admin_protected user) (throw-forbidden!)
                    :else :OK)))

(defn delete-user [{tx :tx :as request}]
  (if-let [user (-> request get-user :body)]
    (do (assert-deletion-permitted! request user)
        (try+
          (if (= [1] (jdbc/delete! tx :users ["id = ?" (:id user)]))
            {:status 204}
            (throw (ex-info "User deleted failed" {:status 500})))
          (catch #(clojure.string/includes?
                    (ex-message %)
                    "violates foreign key constraint")  e
            (throw
              (ex-info
                (str "User delete failed because it violates foreign key constraint: "
                     (ex-message e)) {:status 422} e)))))
    (throw (ex-info "To be deleted user not found." {:status 404}))))


;;; transfer data ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn transfer-data [user-id target-user-id tx]
  (doseq [[table fields] [[:audited_requests [:user_id]]
                          [:contracts [:user_id]]
                          [:customer_orders [:user_id]]
                          [:orders [:user_id]]
                          [:reservations [:user_id :handed_over_by_user_id :returned_to_user_id :delegated_user_id]]]]
    (doseq [field fields]
      (jdbc/update! tx table
                    {(str field) target-user-id}
                    [(str field " = ?") user-id]))))

(defn transfer-data-and-delete-user
  [{{uid :user-id target-user-uid :target-user-uid} :route-params
    tx :tx :as request}]
  (let [target-user (-> target-user-uid (choose-core/find-user-by-some-uid! tx))
        del-user (->> uid
                      user-query
                      sql/format
                      (jdbc/query tx)
                      first)]
    (when-not del-user
      (throw (ex-info "To be deleted user not found." {:status 404})))
    (transfer-data (:id del-user)
                   (:id target-user) tx)
    (delete-user request)))


;;; image handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def IMG-DATA-URL-PREFIX "data:image/jpeg;base64")

(defn assert-proper-image-type! [prefix]
  (when-not (= IMG-DATA-URL-PREFIX prefix)
    (throw (ex-info "Image is not of expected type 'data:image/jpeg;base64'!"
                    {:status 422
                     :body (str "The first chars of img256_url must be equal to data:image/jpeg;base64. "
                                "See also https://tools.ietf.org/html/rfc2397.")}))))

(defn data-url-img->buffered-image ^BufferedImage [data-url-img]
  (let [[img-type img-s] (clojure.string/split data-url-img #"," 2)
        img-ba (.decode (Base64/getDecoder) (.getBytes img-s "UTF-8"))
        in (ByteArrayInputStream. img-ba)]
    (assert-proper-image-type! img-type)
    (ImageIO/read in)))

(defn buffered-image->data-url-img ^String [^BufferedImage img]
  (let [os (ByteArrayOutputStream.)
        _ (ImageIO/write img "jpg" os)
        ba (.toByteArray os)
        base64 (.encodeToString (Base64/getEncoder) ba)]
    (clojure.string/join "," [IMG-DATA-URL-PREFIX base64])))

(defn resized-img ^BufferedImage [^BufferedImage img ^Integer dim]
  (let [img-buffer (BufferedImage. dim dim (.getType img))
        graphics (.createGraphics img-buffer)]
    (.drawImage graphics img 0 0 dim dim nil)
    (.dispose graphics)
    img-buffer))

(defn remove-images [data]
  (assoc data
         :img256_url nil
         :img32_url nil))

(defn set-images [data img-data-url]
  (let [img (data-url-img->buffered-image img-data-url)
        img256-data-url (-> img (resized-img 256) buffered-image->data-url-img)
        img32-data-url (-> img (resized-img 32) buffered-image->data-url-img)]
    (assoc data
           :img256_url img256-data-url
           :img32_url img32-data-url)))

(defn process-images [data]
  (if-let [img-data-url (-> data :img256_url presence)]
    (set-images data img-data-url)
    (if (and (contains? data :img256_url)
             (not (-> data :img256_url presence)))
      (remove-images data)
      data)))


;;; password ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn password-hash
  ([password tx]
   (->> ["SELECT crypt(?,gen_salt('bf',10)) AS pw_hash" password]
        (jdbc/query tx)
        first :pw_hash)))

(defn insert-pw-hash [data tx]
  (if-let [password (-> data :password presence)]
    (assoc data :pw_hash (password-hash password tx))
    data))


;;; update user ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn prepare-write-data [data]
  (-> data
      spy
      (select-keys user-write-keys)
      spy
      (rename-keys user-write-keymap)))

(defn protect-admin! [data user]
  (users-and-groups/assert-attributes-do-not-change!
    data user admin-restricted-attributes)
  (users-and-groups/assert-not-admin-proteced! user))

(defn protect-system-admin! [data user]
  (users-and-groups/assert-attributes-do-not-change!
    data user system-admin-restricted-attributes)
  (users-and-groups/assert-not-system-admin-proteced! user))

(defn protect-attribute-de-escalation!
  [user {data :body :as request}]
  (cond
    (requester-is-system-admin? request) "OK"
    (requester-is-admin?
      request) (users-and-groups/assert-attributes-do-not-change!
                 data user [:is_system_admin :system_admin_protected])
    :else (users-and-groups/assert-attributes-do-not-change!
            data user [:is_system_admin :system_admin_protected
                       :is_admin :admin_protected
                       :organization :org_id])))

(defn patch-user
  ([{tx :tx data :body :as request}]
   (patch-user (prepare-write-data data) tx request))
  ([data tx request]
   (if-let [user (-> request get-user :body)]
     (do (users-and-groups/protect-leihs-core! user)
         (users-and-groups/protect-leihs-core! data)
         (protect-admin-and-system-admin! user request)
         (protect-attribute-de-escalation! user request)
         (or (= [1] (jdbc/update! tx :users data ["id = ?" (:id user)]))
             (throw (ex-info "Number of updated rows does not equal one." {} )))
         (get-user request))
     {:status 404})))


;;; create user ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-user
  ([{tx :tx data :body :as request}]
   (create-user (prepare-write-data data) tx request))
  ([data tx request]
   (users-and-groups/protect-leihs-core! data)
   (when-not (requester-is-admin? request)
     (users-and-groups/assert-attributes-are-not-set!
       data admin-restricted-attributes))
   (when-not (requester-is-system-admin? request)
            (users-and-groups/assert-attributes-are-not-set!
              data system-admin-restricted-attributes))
   (warn data)
   (if-let [user (first (jdbc/insert! tx :users data))]
     {:body user}
     {:status 422
      :body "No user has been created."})))


;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def user-path (path :user {:user-id ":user-id"}))

(def user-transfer-path
  (path :user-transfer-data {:user-id ":user-id"
                             :target-user-uid ":target-user-uid"}))

(def routes
  (cpj/routes
    (cpj/GET user-path [] #'get-user)
    (cpj/PATCH user-path [] #'patch-user)
    (cpj/DELETE user-path [] #'delete-user)
    (cpj/DELETE user-transfer-path [] #'transfer-data-and-delete-user)
    (cpj/POST (path :users) [] #'create-user)))


;#### debug ###################################################################

;(debug/wrap-with-log-debug #'data-url-img->buffered-image)
;(debug/wrap-with-log-debug #'buffered-image->data-url-img)
;(debug/wrap-with-log-debug #'resized-img)

;(debug/debug-ns *ns*)
