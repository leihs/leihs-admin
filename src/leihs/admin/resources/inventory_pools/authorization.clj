(ns leihs.admin.resources.inventory-pools.authorization
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.admin.common.roles.core :as roles]
   [leihs.core.auth.core :refer [http-safe?]]
   [leihs.core.core :refer [str]]
   [taoensso.timbre :refer [error warn info debug spy]]))

(defn some-manager? [role request]
  (if (some
       #(role (-> % :role roles/expand-to-hierarchy set))
       (->> request :authenticated-entity :access-rights))
    true
    false))

(def some-lending-manager? (partial some-manager? :lending_manager))
(def some-inventory-manager? (partial some-manager? :inventory_manager))

(defn some-lending-manager-and-http-safe? [request]
  (and (some-lending-manager? request)
       (http-safe? request)))

(defn some-inventory-manager-and-http-safe? [request]
  (and (some-inventory-manager? request)
       (http-safe? request)))

(defn pool-access-right-for-route [request]
  (let [inventory-pool-id (-> request :route-params :inventory-pool-id)]
    (->> request :authenticated-entity :access-rights
         (filter #(= (:inventory_pool_id %) inventory-pool-id))
         first)))

(defn pool-lending-manager? [request]
  (if-let [access-right (pool-access-right-for-route request)]
    (#{"lending_manager" "inventory_manager"} (:role access-right))
    false))

(defn pool-lending-manager-and-http-safe? [request]
  (and (pool-lending-manager? request)
       (http-safe? request)))

(defn pool-inventory-manager? [request]
  (if-let [access-right (pool-access-right-for-route request)]
    (#{"inventory_manager"} (:role access-right))
    false))

(defn pool-inventory-manager-and-http-safe? [request]
  (and (pool-inventory-manager? request)
       (http-safe? request)))

;#### debug ###################################################################

;(debug/wrap-with-log-debug #'activity-filter)
;(debug/wrap-with-log-debug #'set-order)
;(debug/wrap-with-log-debug #'inventory-pools-query)
;(debug/wrap-with-log-debug #'inventory-pools-formated-query)

;(-> *request* :route-params :inventory-pool-id)

;(pool-access-right-for-route *request*)
;(pool-lending-manager? *request*)

;(debug/debug-ns *ns*)
