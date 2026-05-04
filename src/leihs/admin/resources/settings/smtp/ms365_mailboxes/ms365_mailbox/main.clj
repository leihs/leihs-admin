(ns leihs.admin.resources.settings.smtp.ms365-mailboxes.ms365-mailbox.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(defn mailbox-query [mailbox-id]
  (-> (sql/select :id :token_expires_at :created_at :updated_at)
      (sql/from :ms365_mailboxes)
      (sql/where [:= :id mailbox-id])))

(defn get-mailbox [{tx :tx {mailbox-id :mailbox-id} :route-params}]
  (if-let [mailbox (-> (mailbox-query mailbox-id)
                       sql-format
                       (->> (jdbc-query tx))
                       first)]
    {:body mailbox}
    {:status 404}))

(defn delete-mailbox [{tx :tx {mailbox-id :mailbox-id} :route-params}]
  (let [deleted-id (:id (jdbc/execute-one!
                         tx
                         ["DELETE FROM ms365_mailboxes WHERE id = ?" mailbox-id]
                         {:return-keys true}))]
    (if deleted-id
      {:status 204}
      {:status 404 :body "Mailbox not found"})))

(defn routes [request]
  (case (:request-method request)
    :get (get-mailbox request)
    :delete (delete-mailbox request)))
