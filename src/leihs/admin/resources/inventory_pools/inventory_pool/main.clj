(ns leihs.admin.resources.inventory-pools.inventory-pool.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [bidi.bidi :refer [match-route]]
   [clojure.core.match :refer [match]]
   [clojure.set :as set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.paths :refer [paths]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [delete! insert! query update!]
    :rename {query jdbc-query,
             delete! jdbc-delete!
             update! jdbc-update!
             insert! jdbc-insert!}]))

(def base-fields #{:inventory_pools.id
                   :inventory_pools.name
                   :inventory_pools.shortname
                   :inventory_pools.email
                   :inventory_pools.description})

(def extra-fields #{:inventory_pools.default_contract_note
                    :inventory_pools.print_contracts
                    :inventory_pools.automatic_suspension
                    :inventory_pools.automatic_suspension_reason
                    :inventory_pools.required_purpose})

(def create-fields (set/union base-fields #{:inventory_pools.is_active}))
(def patch-fields (set/union base-fields extra-fields))
(def get-fields (set/union create-fields
                           extra-fields
                           #{:workdays.reservation_advance_days}))

(defn inventory-pool [tx id]
  (-> (apply sql/select get-fields)
      (sql/from :inventory-pools)
      (sql/where [:= :inventory_pools.id id])
      (sql/join :workdays [:= :workdays.inventory_pool_id :inventory_pools.id])
      sql-format
      (->> (jdbc-query tx))
      first))

(defn get-inventory-pool
  [{{inventory-pool-id :inventory-pool-id} :route-params tx :tx :as request}]
  {:body (inventory-pool tx inventory-pool-id)})

(defn create-inventory-pool [{tx :tx data :body :as request}]
  (if-let [id (->> (select-keys data create-fields)
                   (jdbc-insert! tx :inventory_pools)
                   :id)]
    (do (when-let [rad (:reservation_advance_days data)]
          (jdbc-update! tx :inventory_pools
                        {:reservation_advance_days rad}
                        ["inventory_pool_id = ?" id]))
        {:status 201, :body (inventory-pool tx id)})
    {:status 422
     :body "No inventory-pool has been created."}))

(defn patch-inventory-pool
  [{{inventory-pool-id :inventory-pool-id} :route-params
    tx :tx data :body :as request}]
  (when (->> ["SELECT true AS exists FROM inventory_pools WHERE id = ?" inventory-pool-id]
             (jdbc-query tx)
             first :exists)
    (jdbc-update! tx :inventory_pools
                  (select-keys data patch-fields)
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
      :get (get-inventory-pool request)
      :delete (delete-inventory-pool request)
      :patch (patch-inventory-pool request))))
