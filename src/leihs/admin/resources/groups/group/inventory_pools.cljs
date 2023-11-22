(ns leihs.admin.resources.groups.group.inventory-pools
  (:refer-clojure :exclude [str keyword])
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.roles.core :as roles]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.group.core :as group.shared]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [str]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Alert]]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn prepare-inventory-pools-data [data]
  (->> data
       (reduce (fn [roles role]
                 (-> roles
                     (assoc-in [(:inventory_pool_id role) :name] (:inventory_pool_name role))
                     (assoc-in [(:inventory_pool_id role) :id] (:inventory_pool_id role))
                     (assoc-in [(:inventory_pool_id role) :key] (:inventory_pool_id role))))
                    ; (assoc-in [(:inventory_pool_id role) :role (:role role)] role)

               {})
       (map (fn [[_ v]] v))
       (sort-by :name)
       (into [])))

(defonce fetch-inventory-pools-roles-id* (reagent/atom nil))

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

(defn inventory-pools-roles-debug-component []
  [:div
   (when (:debug @state/global-state*)
     [:div.inventory-pools-roles-debug
      [:hr]
      [:div.inventory-pools-roles-data
       [:h3 "@data*"]
       [:pre (with-out-str (pprint @data*))]]])
   [group.shared/debug-component]])

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
                 (map str)
                 (clojure.string/join ", "))]]])}]
     [:> Alert {:variant "secondary" :className "mt-3"}
      "Not part of any Inventory Pool"])])
