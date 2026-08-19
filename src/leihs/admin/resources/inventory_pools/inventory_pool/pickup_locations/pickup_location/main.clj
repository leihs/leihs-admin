(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.main :as pickup-locations]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [query update! delete!] :rename {query jdbc-query
                                                          update! jdbc-update!
                                                          delete! jdbc-delete!}]))

(defn pickup-location
  [{{:keys [inventory-pool-id pickup-location-id]} :route-params tx :tx}]
  (if-let [pickup-location (-> (pickup-locations/base-query inventory-pool-id)
                               (sql/where [:= :id pickup-location-id])
                               sql-format
                               (->> (jdbc-query tx))
                               first)]
    {:body pickup-location}
    {:status 404}))

(defn patch-pickup-location
  [{tx :tx
    {:keys [inventory-pool-id pickup-location-id]} :route-params
    body :body}]
  (let [patch-data (merge {:name (:name body) :description (:description body)}
                          (select-keys body [:active]))]
    (if (= 1 (::jdbc/update-count
              (jdbc-update! tx :pickup_locations patch-data
                            ["id = ? AND inventory_pool_id = ?" pickup-location-id inventory-pool-id])))
      {:status 204}
      {:status 404})))

(defn has-reservations? [tx pickup-location-id]
  (-> (sql/select [[:exists
                    (-> (sql/select 1)
                        (sql/from :reservations)
                        (sql/where [:= :pickup_location_id pickup-location-id]))]])
      sql-format
      (->> (jdbc-query tx))
      first :exists))

(defn delete-pickup-location
  [{tx :tx {:keys [inventory-pool-id pickup-location-id]} :route-params}]
  (if (has-reservations? tx pickup-location-id)
    {:status 422
     :body (str "This pickup location cannot be deleted because it is"
                " referenced by reservations. Deactivate it instead.")}
    (if (= 1 (::jdbc/update-count
              (jdbc-delete! tx :pickup_locations
                            ["id = ? AND inventory_pool_id = ?" pickup-location-id inventory-pool-id])))
      {:status 204}
      {:status 404})))

(defn routes [request]
  (case (:request-method request)
    :get (pickup-location request)
    :patch (patch-pickup-location request)
    :delete (delete-pickup-location request)))
