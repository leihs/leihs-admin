(ns leihs.admin.resources.audits.changes.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.audits.changes.shared :refer [default-query-params]]
   [leihs.admin.utils.sql :refer [where-with-sanitized-uuid]]
   [leihs.core.core :refer [keyword presence str]]
   [leihs.core.routing.back :as routing :refer [mixin-default-query-params
                                                set-per-page-and-offset]]
   [leihs.core.uuid :refer [uuid]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]))

(defn audited-changes-meta
  [{tx :tx :as request}]
  {:body
   {:tables
    (->> ["SELECT DISTINCT table_name FROM audited_changes"]
         (jdbc-query tx)
         (map :table_name))}})

(def request-sub
  (-> (sql/select [:audited_requests.id :request_id])
      (sql/from :audited_requests)
      (sql/where
       [:or
        [:= :audited_changes.txid :audited_requests.txid]
        [:= :audited_changes.txid :audited_requests.tx2id]])))

(def selects
  [:audited-changes.id
   :audited-changes.txid
   :audited-changes.tg_op
   :audited-changes.table_name
   :audited-changes.created_at
   :audited-changes.pkey
   [request-sub :request_id]])

(def changes-base-query
  (-> (apply sql/select selects)
      (sql/select
       [[:array_to_json
         [:ARRAY
          (sql/select
           [[:jsonb_object_keys :audited_changes.changed]])]]
        :changed_attributes])
      (sql/from :audited_changes)
      (sql/order-by [:audited_changes.created_at :desc]
                    [:audited_changes.table_name :asc]
                    [:audited_changes.pkey :asc])))

(comment
  (-> changes-base-query
      (sql-format)))

(defn filter-by-search-term
  [query {{term :term} :query-params :as request}]
  (if-let [term (presence term)]
    (-> query
        (sql/where [(keyword "@@") :audited_changes.changed term]))
    query))

(defn filter-by-table
  [query {{table-name :table} :query-params :as request}]
  (if-not (presence table-name)
    query
    (-> query
        (sql/where
         [:= :audited_changes.table_name table-name]))))

(defn filter-by-txid [query {{txid :txid} :query-params}]
  (if-let [txid (presence txid)]
    (where-with-sanitized-uuid query :audited_changes.txid txid)
    query))

(defn filter-by-request-id [query {{request-id :request-id} :query-params}]
  (if-let [request-id (some-> request-id presence uuid)]
    (-> query
        (sql/join :audited_requests [:= :audited_requests.id request-id])
        (sql/where [:or
                    [:= :audited_changes.txid :audited_requests.txid]
                    [:= :audited_changes.txid :audited_requests.tx2id]]))
    query))

(defn filter-by-pkey [query {{pkey :pkey} :query-params}]
  (if-let [pkey (presence pkey)]
    (sql/where query [:= :audited_changes.pkey (str pkey)])
    query))

(defn filter-by-tg-op [query {{tg-op :tg-op} :query-params}]
  (if-let [tg-op (presence tg-op)]
    (sql/where query [:= :audited_changes.tg_op tg-op])
    query))

(defn audited-changes [{tx :tx :as request}]
  {:body
   {:meta {:tables
           (->> ["SELECT DISTINCT table_name FROM audited_changes"]
                (jdbc-query tx)
                (map :table_name))}
    :changes (-> changes-base-query
                 (set-per-page-and-offset request)
                 (filter-by-search-term request)
                 (filter-by-txid request)
                 (filter-by-request-id request)
                 (filter-by-pkey request)
                 (filter-by-tg-op request)
                 (filter-by-table request)
                 sql-format
                 (->> (jdbc/execute! tx)))}})

(comment
  (-> changes-base-query
      (filter-by-search-term {})
      (filter-by-txid {})
      (filter-by-request-id {})
      (filter-by-pkey {})
      (filter-by-tg-op {})
      (filter-by-table {})
      (sql-format :inline true :pretty true)
      println))

(defn routes [request]
  (case (:request-method request)
    :get (-> request
             (mixin-default-query-params default-query-params)
             audited-changes)))

;#### debug ###################################################################

;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'users-formated-query)
;(debug/wrap-with-log-debug #'users-formated-query)
