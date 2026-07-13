(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [next.jdbc.sql :as jdbc]))

(def fields #{:id :inventory_pool_id :name :description :created_at :updated_at})

(defn base-query [inventory-pool-id]
  (-> (apply sql/select fields)
      (sql/from :pickup_locations)
      (sql/where [:= :inventory_pool_id inventory-pool-id])
      (sql/order-by :name)))

(defn pickup-locations
  [{{inventory-pool-id :inventory-pool-id} :route-params tx :tx}]
  {:body
   {:pickup-locations
    (-> (base-query inventory-pool-id) sql-format (->> (jdbc/query tx)))}})

(defn create-pickup-location
  [{tx :tx
    {inventory-pool-id :inventory-pool-id} :route-params
    {:keys [name description]} :body}]
  (if-let [pickup-location (jdbc/insert! tx :pickup_locations
                                         {:inventory_pool_id inventory-pool-id
                                          :name name
                                          :description description})]
    {:status 201 :body pickup-location}
    {:status 422 :body "The pickup location could not be created!"}))

(defn routes [request]
  (case (:request-method request)
    :get (pickup-locations request)
    :post (create-pickup-location request)))
