(ns leihs.admin.resources.settings.smtp.main
  (:require
   [clojure.string :as str]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.utils.jdbc :as utils-jdbc]
   [next.jdbc :as jdbc]
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

(defn get-emails [{tx :tx query-params :query-params}]
  (let [limit (or (:limit query-params) 50)
        offset (or (:offset query-params) 0)
        total-count (-> (sql/select :%count.*)
                        (sql/from :emails)
                        sql-format
                        (->> (jdbc-query tx) first :count))
        emails (-> (sql/select :*)
                   (sql/from :emails)
                   (sql/order-by [:created_at :desc])
                   (sql/limit limit)
                   (sql/offset offset)
                   sql-format
                   (->> (jdbc-query tx)))]
    {:body {:emails emails
            :total total-count}}))

(defn send-test-email [{tx :tx data :body}]
  (let [email-record (-> data
                         (select-keys [:from_address :to_address :subject :body])
                         (assoc :user_id nil
                                :inventory_pool_id nil))]
    (jdbc-insert! tx :emails email-record)
    {:body {:message "Test email queued for sending"}
     :status 201}))

(def smtp-settings-path (path :smtp-settings))

(defn routes [request]
  (case (:request-method request)
    :get (get-smtp-settings request)
    :patch (upsert request)
    :put (upsert request)))

;#### debug ###################################################################

;(debug/debug-ns *ns*)
