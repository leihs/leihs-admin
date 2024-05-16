(ns leihs.admin.resources.inventory-pools.inventory-pool.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [delete! insert! query update!]
    :rename {query jdbc-query,
             delete! jdbc-delete!
             update! jdbc-update!
             insert! jdbc-insert!}]))

(def fields #{:id
              :name
              :description
              :default_contract_note
              :shortname
              :email
              :print_contracts
              :automatic_suspension
              :automatic_suspension_reason
              :required_purpose
              :is_active})

(defn inventory-pool
  [{{inventory-pool-id :inventory-pool-id} :route-params tx :tx :as request}]
  {:body (-> (apply sql/select fields)
             (sql/from :inventory-pools)
             (sql/where [:= :id inventory-pool-id])
             sql-format
             (->> (jdbc-query tx))
             first)})

(defn create-inventory-pool [{tx :tx data :body :as request}]
  (if-let [inventory-pool (jdbc-insert! tx :inventory_pools
                                        (select-keys data fields))]
    {:status 201, :body inventory-pool}
    {:status 422
     :body "No inventory-pool has been created."}))

(defn patch-inventory-pool
  [{{inventory-pool-id :inventory-pool-id} :route-params
    tx :tx data :body :as request}]
  (when (->> ["SELECT true AS exists FROM inventory_pools WHERE id = ?" inventory-pool-id]
             (jdbc-query tx)
             first :exists)
    (jdbc-update! tx :inventory_pools
                  (select-keys data fields)
                  ["id = ?" inventory-pool-id])
    {:status 204}))

(defn delete-inventory-pool [{tx :tx {inventory-pool-id :inventory-pool-id} :route-params}]
  (assert inventory-pool-id)
  (if (= 1 (::jdbc/update-count
            (jdbc-delete! tx :inventory_pools ["id = ?" inventory-pool-id])))
    {:status 204}
    {:status 404 :body "Delete inventory-pool failed without error."}))

(def routes
  (fn [request]
    (case (:request-method request)
      :get (inventory-pool request)
      :delete (delete-inventory-pool request)
      :patch (patch-inventory-pool request))))
