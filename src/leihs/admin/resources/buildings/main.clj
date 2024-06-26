(ns leihs.admin.resources.buildings.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.buildings.building.main :as building]
   [leihs.admin.resources.buildings.shared :as shared]
   [leihs.admin.utils.seq :as seq]
   [leihs.core.core :refer [presence str]]
   [leihs.core.uuid :refer [uuid]]
   [next.jdbc.sql :as jdbc]))

(def count-items-select-query
  (-> (sql/select :%count.*)
      (sql/from :items)
      (sql/join :rooms [:= :rooms.id :items.room_id])
      (sql/join [:buildings :buildings_2] [:= :buildings_2.id :rooms.building_id])
      (sql/where := :rooms.building_id :buildings.id)))

(def count-rooms-select-query
  (-> (sql/select :%count.*)
      (sql/from :rooms)
      (sql/join [:buildings :buildings_2] [:= :buildings_2.id :rooms.building_id])
      (sql/where := :rooms.building_id :buildings.id)))

(def buildings-base-query
  (-> (sql/select :buildings.id
                  :buildings.name
                  :buildings.code
                  shared/is-general-select-expr
                  [count-items-select-query :items_count]
                  [count-rooms-select-query :rooms_count])
      (sql/from :buildings)
      (sql/order-by :buildings.name)))

(defn set-per-page-and-offset
  ([query {per-page :per-page page :page}]
   (when (or (-> per-page presence not)
             (-> per-page integer? not)
             (> per-page 1000)
             (< per-page 1))
     (throw (ex-info "The query parameter per-page must be present and set to an integer between 1 and 1000."
                     {:status 422})))
   (when (or (-> page presence not)
             (-> page integer? not)
             (< page 0))
     (throw (ex-info "The query parameter page must be present and set to a positive integer."
                     {:status 422})))
   (set-per-page-and-offset query per-page page))
  ([query per-page page]
   (-> query
       (sql/limit per-page)
       (sql/offset (* per-page (- page 1))))))

(def searchable-expr [:concat :name " " :code])

(defn term-filter [query request]
  (if-let [term (-> request :query-params-raw :term presence)]
    (-> query
        (sql/where [:ilike searchable-expr (str "%" term "%")]))
    query))

(defn inventory-pool-filter [query request]
  (if-let [pool-id (-> request :query-params-raw :inventory_pool_id presence)]
    (-> query
        (sql/join :items [:= :items.building_id :buildings.id])
        (sql/join :inventory_pools [:= :items.inventory_pool_id :inventory_pools.id])
        (sql/where [:= :inventory_pools.id (uuid pool-id)]))
    query))

(defn buildings-query [request]
  (let [query-params  (-> request :query-params
                          shared/normalized-query-parameters)]
    (-> buildings-base-query
        (set-per-page-and-offset query-params)
        (term-filter request))))

(defn buildings [{tx :tx :as request}]
  (let [query (buildings-query request)
        offset (:offset query)]
    {:body
     {:buildings (-> query
                     sql-format
                     (->> (jdbc/query tx)
                          (seq/with-index offset)
                          seq/with-page-index))}}))

;;; create building ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-building [{tx :tx data :body :as request}]
  (if-let [building (jdbc/insert! tx
                                  :buildings
                                  (select-keys data building/fields))]
    {:status 201, :body building}
    {:status 422
     :body "No building has been created."}))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn routes [request]
  (case (:request-method request)
    :get (buildings request)
    :post (create-building request)))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'groups-formated-query)
