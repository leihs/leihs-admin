(ns leihs.admin.resources.categories.category.models.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.inventory-pools.shared :as pools]
   [leihs.core.db :as db]
   [next.jdbc.sql :as jdbc]))

(defn used-in-pools [tx id]
  (-> (sql/select-distinct :pools.id, :pools.name)
      (sql/from [:inventory_pools :pools])
      (sql/join :items [:= :items.inventory_pool_id :pools.id])
      (sql/join :models [:= :models.id :items.model_id])
      (sql/where [:= :models.id id])
      (sql/where [:= :pools.is_active true])
      (sql/where [:= :items.retired nil])
      sql-format
      (->> (jdbc/query tx))))

(defn models [tx id]
  (-> (sql/select :models.id
                  [[:trim [:|| :models.product " "
                           [:coalesce :models.version ""]]]
                   :name])
      (sql/from :models)
      (sql/join :model_links
                [:= :model_links.model_id :models.id])
      (sql/where [:= :model_links.model_group_id id])
      (sql/order-by :models.product :models.version)
      sql-format
      (->> (jdbc/query tx))
      (->> (map #(assoc % :used-in-pools (used-in-pools tx (:id %)))))))

(comment (let [tx (db/get-ds)
               category-id #uuid "ffcfdbcd-1d0b-5ec9-9715-3b2e631d2da9"
               model-id #uuid "926f89e5-c241-43f4-8342-1adea7bb4a65"]
           (models tx category-id)
          ; (used-in-pools tx model-id)
           ))

(defn index
  [{tx :tx {category-id :category-id} :route-params :as request}]
  {:body {:models (models tx category-id)}})

(defn routes [request]
  (case (:request-method request)
    :get (index request)
    (throw (ex-info "Method not allowed" {:status 405}))))
