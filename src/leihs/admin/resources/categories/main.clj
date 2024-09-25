(ns leihs.admin.resources.categories.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.utils.pagination :as page]
   [leihs.admin.utils.query-params :as shared]
   [leihs.admin.utils.seq :as seq]
   [leihs.core.db :as db]
   [next.jdbc.sql :as jdbc]))

(def base-query
  (-> (sql/select :model_groups.id
                  :model_groups.name)
      (sql/from :model_groups)
      (sql/where [:= :model_groups.type "Category"])
      (sql/order-by :model_groups.name)))

; (defn term-filter [query request]
;   (if-let [term (-> request :query-params-raw :term presence)]
;     (-> query
;         (sql/where [:ilike :model_groups.name (str "%" term "%")]))
;     query))

(defn query [request]
  (let [query-params (-> request
                         :query-params
                         shared/normalized-query-parameters)]
    (-> base-query
        (page/set-per-page-and-offset query-params)
        #_(term-filter request))))

(defn index [{tx :tx :as request}]
  (let [offset (:offset query)]
    {:body
     {:categories (-> request query
                      sql-format
                      (->> (jdbc/query tx)
                           (seq/with-index offset)
                           seq/with-page-index))}}))

;;; create category ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create [{tx :tx data :body :as request}]
  (if-let [category (jdbc/insert! tx
                                  :model_groups
                                  (-> data
                                      (select-keys [:name])
                                      (assoc :type "Category")))]
    {:status 201, :body category}
    {:status 422
     :body "No category has been created."}))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn routes [request]
  (case (:request-method request)
    :get (index request)
    :post (create request)))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'groups-formated-query)
