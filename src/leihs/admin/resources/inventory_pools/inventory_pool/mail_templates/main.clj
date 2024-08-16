(ns leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.main
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.mail-templates.main :as global]
   [leihs.admin.resources.mail-templates.shared :as shared]
   [leihs.admin.utils.seq :as seq]
   [next.jdbc.sql :as jdbc]))

(defn mail-templates-base-query [inventory-pool-id]
  (-> (sql/select :*)
      (sql/from :mail-templates)
      (sql/where [:= :is_template_template false])
      (sql/where [:= :inventory_pool_id inventory-pool-id])
      (sql/order-by :name :language_locale)))

(defn mail-templates-query [{{:keys [inventory-pool-id]} :route-params :as request}]
  (let [query-params (-> request :query-params
                         shared/normalized-query-parameters)]
    (-> (mail-templates-base-query inventory-pool-id)
        (global/set-per-page-and-offset query-params)
        (global/term-filter request)
        (global/name-filter request)
        (global/language-locale-filter request)
        (global/type-filter request))))

(defn mail-templates [{tx :tx :as request}]
  (let [query (mail-templates-query request)
        offset (:offset query)]
    {:body
     {:mail-templates (-> query
                          sql-format
                          (->> (jdbc/query tx)
                               (seq/with-index offset)
                               seq/with-page-index))}}))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn routes [request]
  (case (:request-method request)
    :get (mail-templates request)))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'groups-formated-query)
