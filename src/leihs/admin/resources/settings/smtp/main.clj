(ns leihs.admin.resources.settings.smtp.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.utils.jdbc :as utils-jdbc]
   [leihs.core.core :refer [presence]]
   [next.jdbc.sql :refer [query insert!] :rename {query jdbc-query insert! jdbc-insert!}]))

(defn get-smtp-settings [{tx :tx}]
  {:body (-> (sql/select :*)
             (sql/from :smtp_settings)
             sql-format
             (->> (jdbc-query tx) first)
             (or (throw (ex-info "smtp-settings not found" {:status 404})))
             (dissoc :id))})

(defn upsert [{tx :tx data :body :as request}]
  (utils-jdbc/insert-or-update! tx :smtp_settings ["id = 0"] data)
  (-> (get-smtp-settings request) (assoc :status 200)))

(defn term-filter [query term]
  (if-let [term (presence term)]
    (let [term-like (str "%" term "%")]
      (sql/where query
                 [:or
                  [:ilike [:cast :user_id :text] term-like]
                  [:ilike [:cast :inventory_pool_id :text] term-like]
                  [:ilike [:cast :source_pool_id :text] term-like]
                  [:ilike :from_address term-like]
                  [:ilike :to_address term-like]
                  [:ilike :subject term-like]
                  [:ilike :body term-like]
                  [:ilike :error_message term-like]]))
    query))

(defn template-filter [query template]
  (cond
    (= template "none") (sql/where query [:= :template nil])
    (presence template) (sql/where query [:= :template template])
    :else query))

(defn state-filter [query state]
  (case state
    "success" (sql/where query [:= true :is_successful])
    "failure" (sql/where query [:= false :is_successful])
    query))

(defn emails-base-query [query query-params]
  (-> query
      (sql/from :emails)
      (term-filter (:term query-params))
      (template-filter (:template query-params))
      (state-filter (:state query-params))))

(defn get-emails [{tx :tx query-params :query-params}]
  (let [limit (or (:limit query-params) 50)
        offset (or (:offset query-params) 0)
        total-count (-> (sql/select :%count.*)
                        (emails-base-query query-params)
                        sql-format
                        (->> (jdbc-query tx) first :count))
        emails (-> (sql/select :*)
                   (emails-base-query query-params)
                   (sql/order-by [:created_at :desc])
                   (sql/limit limit)
                   (sql/offset offset)
                   sql-format
                   (->> (jdbc-query tx)))
        templates (-> (sql/select-distinct :name)
                      (sql/from :mail_templates)
                      (sql/where [:= :is_template_template true])
                      (sql/order-by :name)
                      sql-format
                      (->> (jdbc-query tx) (map :name)))]
    {:body {:emails emails
            :total total-count
            :templates templates}}))

(defn get-email [{tx :tx {email-id :email-id} :route-params}]
  (if-let [email (-> (sql/select :*)
                     (sql/from :emails)
                     (sql/where [:= :id email-id])
                     sql-format
                     (->> (jdbc-query tx) first))]
    {:body email}
    {:status 404}))

(defn send-test-email [{tx :tx data :body {user-id :id} :authenticated-entity}]
  (let [email-record (-> data
                         (select-keys [:from_address :to_address :subject :body])
                         (assoc :user_id user-id
                                :inventory_pool_id nil))]
    (jdbc-insert! tx :emails email-record)
    {:body {:message "Test email queued for sending"}
     :status 201}))

(defn routes [request]
  (case (:request-method request)
    :get (get-smtp-settings request)
    :patch (upsert request)
    :put (upsert request)))

;#### debug ###################################################################

;(debug/debug-ns *ns*)
