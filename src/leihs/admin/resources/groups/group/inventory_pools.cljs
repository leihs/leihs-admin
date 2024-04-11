(ns leihs.admin.resources.groups.group.inventory-pools
  (:require [cljs.core.async :as async :refer [<! go]]
            [clojure.string :refer [join]]
            [leihs.admin.common.components.table :as table]
            [leihs.admin.common.http-client.core :as http-client]
            [leihs.admin.common.roles.core :as roles]
            [leihs.admin.paths :as paths :refer [path]]
            [leihs.core.routing.front :as routing]
            [react-bootstrap :as react-bootstrap :refer [Alert]]
            [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn fetch-inventory-pools-roles []
  (go (reset! data*
              (some->
               {:chan (async/chan)
                :url (path :group-inventory-pools-roles
                           (-> @routing/state* :route-params))}
               http-client/request :chan <!
               http-client/filter-success!
               :body :inventory_pools_roles))))

(defn clean-and-fetch [& args]
  (reset! data* nil)
  (fetch-inventory-pools-roles))

(defn table-component []
  [:div
   [routing/hidden-state-component
    {:did-change clean-and-fetch}]
   (if (seq @data*)
     [table/container
      {:borders false
       :className "group"
       :header [:tr [:th "Pool"] [:th "Roles"]]
       :body
       (for [row (->>  @data*
                       (sort-by :inventory_pool_name))]
         [:tr.pool {:key (:inventory_pool_id row)}
          [:td
           [:a {:href (path :inventory-pool
                            {:inventory-pool-id (:inventory_pool_id row)})}
            [:<> (:inventory_pool_name row)]] ""]
          [:td
           [:a {:href (path :inventory-pool-group-roles
                            {:inventory-pool-id (:inventory_pool_id row)
                             :group-id (:group_id row)})}
            (->> row :role roles/expand-to-hierarchy
                 (map name)
                 (join ", "))]]])}]
     [:> Alert {:variant "secondary" :className "mt-3"}
      "Not part of any Inventory Pool"])])
