(ns leihs.admin.resources.settings.smtp.ms365-mailboxes.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(def mailboxes-query
  (-> (sql/select :id :token_expires_at :created_at :updated_at)
      (sql/from :ms365_mailboxes)
      (sql/order-by [:created_at :desc])))

(defn get-mailboxes [{tx :tx}]
  {:body {:mailboxes (-> mailboxes-query
                         sql-format
                         (->> (jdbc-query tx)))}})

(defn routes [request]
  (case (:request-method request)
    :get (get-mailboxes request)))
