(ns leihs.admin.resources.groups.group.inventory-pools
  (:refer-clojure :exclude [str keyword])
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [clojure.core :as core]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.roles.components :refer [put-roles<
                                                roles-component]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.group.core :as group-core]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Alert]]
   [reagent.core :as reagent]))

(defn get-roles [role]
  (case role
    :customer           {:customer true, :group_manager false, :lending_manager false, :inventory_manager false}
    :group_manager      {:customer true, :group_manager true, :lending_manager false, :inventory_manager false}
    :lending_manager    {:customer true, :group_manager true, :lending_manager true, :inventory_manager false}
    :inventory_manager  {:customer true, :group_manager true, :lending_manager true, :inventory_manager true}))

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

(defn roles-update-handler [roles row]
  (go (<! (put-roles<
           (path :inventory-pool-group-roles
                 {:inventory-pool-id (:inventory_pool_id row)
                  :group-id (:group_id row)})
           roles))
      (clean-and-fetch)))

(defn table-component []
  [:div
   [routing/hidden-state-component
    {:did-change clean-and-fetch}]
   (if (seq @data*)
     (let [data @group-core/data*]
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
             [roles-component
              (get-roles (clojure.core/keyword (:role row)))
              :compact true
              :message (core/str (:users_count data))
              :update-handler #(roles-update-handler % row)
              :label "Role"
              :query-params-key :role
              :default-option "customer"]]])}])
     [:> Alert {:variant "secondary" :className "mt-3"}
      "Not part of any Inventory Pool"])])
