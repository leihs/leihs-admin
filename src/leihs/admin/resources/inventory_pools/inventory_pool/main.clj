(ns leihs.admin.resources.inventory-pools.inventory-pool.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [bidi.bidi :refer [match-route]]
   [clojure.core.match :refer [match]]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.paths :refer [paths]]
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

(defn workdays
  [{{inventory-pool-id :inventory-pool-id} :route-params tx :tx :as request}]
  {:body (-> (sql/select :*)
             (sql/from :workdays)
             (sql/where [:= :inventory_pool_id inventory-pool-id])
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

(defn routes [request]
  (let [handler-key (->> request :uri (match-route paths) :handler)]
    (match [(:request-method request) handler-key]
      [:get :inventory-pool] (inventory-pool request)
      [:get :inventory-pool-workdays] (workdays request)
      [:patch :inventory-pool-workdays] (patch-workdays request)
      [:delete :inventory-pool] (delete-inventory-pool request)
      [:patch :inventory-pool] (patch-inventory-pool request))))
