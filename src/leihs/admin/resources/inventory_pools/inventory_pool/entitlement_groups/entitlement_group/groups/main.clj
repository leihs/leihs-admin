(ns leihs.admin.resources.inventory-pools.inventory-pool.entitlement_groups.entitlement_group.groups.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [compojure.core :as cpj]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.common.membership.groups.main :refer [extend-with-membership]]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.resources.groups.main :as groups]
   [leihs.admin.utils.jdbc :as utils.jdbc]
   [leihs.admin.utils.seq :as seq]
   [next.jdbc.sql :refer [delete! query] :rename {query jdbc-query, delete! jdbc-delete!}]))

;;; groups ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn member-expr [entitlement-group-id]
  [:exists
   (-> (sql/select true)
       (sql/from :entitlement_groups_groups)
       (sql/where [:= :groups.id :entitlement_groups_groups.group_id])
       (sql/where [:= :entitlement_groups_groups.entitlement_group_id entitlement-group-id]))])

(defn groups-query
  [{{entitlement-group-id :entitlement-group-id} :route-params :as request}]
  (-> (groups/groups-query request)
      (extend-with-membership  (member-expr entitlement-group-id) request)
      (sql/select [entitlement-group-id :entitlement_group_id])))

(defn groups-formated-query [request]
  (-> request groups-query sql-format))

(defn groups [{tx :tx-next :as request}]
  (let [query (groups-query request)
        offset (:offset query)]
    {:body
     {:groups (-> query sql-format
                  (->> (jdbc-query tx)
                       (seq/with-index offset)
                       seq/with-page-index))}}))

;;; add ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-entitlement-group-group
  [{{inventory-pool-id :inventory-pool-id
     entitlement-group-id :entitlement-group-id
     group-id :group-id} :route-params
    tx :tx-next :as request}]
  (utils.jdbc/insert-or-update!
   tx :entitlement_groups_groups
   ["entitlement_group_id = ? AND group_id = ?  " entitlement-group-id group-id]
   {:entitlement_group_id entitlement-group-id :group_id group-id})
  {:status 204})

;;; remove ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn remove-entitlement-group-group
  [{{inventory-pool-id :inventory-pool-id
     entitlement-group-id :entitlement-group-id
     group-id :group-id} :route-params
    tx :tx-next :as request}]
  (if (= [1] (jdbc-delete! tx :entitlement_groups_groups
                           ["entitlement_group_id = ? AND group_id = ?
                            " entitlement-group-id group-id]))
    {:status 204}
    {:status 404 :body "Remove entitlement group failed without error."}))

;;; routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def groups-path
  (path
   :inventory-pool-entitlement-group-groups
   {:inventory-pool-id ":inventory-pool-id"
    :entitlement-group-id ":entitlement-group-id"}))

(def group-path
  (path
   :inventory-pool-entitlement-group-group
   {:inventory-pool-id ":inventory-pool-id"
    :entitlement-group-id ":entitlement-group-id"
    :group-id ":group-id"}))

(def routes
  (cpj/routes
   (cpj/GET groups-path [] #'groups)
   (cpj/PUT group-path [] #'add-entitlement-group-group)
   (cpj/DELETE group-path [] #'remove-entitlement-group-group)))

;#### debug ###################################################################

;(debug/wrap-with-log-debug #'filter-suspended)
;(debug/wrap-with-log-debug #'groups-formated-query)
;(debug/debug-ns *ns*)
