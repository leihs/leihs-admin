(ns leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.core
  (:require [cljs.core.async :as async :refer [<! go]]
            [leihs.admin.common.components.navigation.breadcrumbs :as breadcrumbs]
            [leihs.admin.paths :as paths :refer [path]]
            [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
            [leihs.core.core :refer [presence]]
            [leihs.core.requests.core :as requests]
            [leihs.core.routing.front :as routing]
            [react-bootstrap :as react-bootstrap]
            [reagent.core :as reagent :refer [reaction]]))

(defonce id*
  (reaction (or (-> @routing/state* :route-params :entitlement-group-id presence)
                ":entitlement-group-id")))

(defonce data* (reagent/atom nil))

(defonce path*
  (reaction
   (path :inventory-pool-entitlement-group
         {:inventory-pool-id @inventory-pool/id*
          :entitlement-group-id @id*})))

(defn fetch []
  (let [ch (async/chan)]
    (requests/send-off {:url @path*} {} :chan ch)
    (go (let [resp (<! ch)]
          (when (:success resp)
            (reset! data* (-> resp :body)))))))

(defn entitlement-group-name []
  [:<>
   [routing/hidden-state-component
    {:did-change fetch}]
   (let [inner (when @data*
                 [:<> (str (:name @data*))])]
     [:<> inner])])

(defn tabs [active]
  [:> react-bootstrap/Nav {:className "mb-3"
                           :justify false
                           :variant "tabs"
                           :defaultActiveKey active}
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link

     (let [href (path :inventory-pool-entitlement-group
                      {:inventory-pool-id @inventory-pool/id*
                       :entitlement-group-id @id*})]
       {:active (= (:path @routing/state*) href)
        :href href})
     "Overview"]]
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     (let [href (path :inventory-pool-entitlement-group-users
                      {:inventory-pool-id @inventory-pool/id*
                       :entitlement-group-id @id*})]
       {:active (= (:path @routing/state*) href)
        :href href})
     "Users"]]
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     (let [href (path :inventory-pool-entitlement-group-groups
                      {:inventory-pool-id @inventory-pool/id*
                       :entitlement-group-id @id*})]
       {:active (= (:path @routing/state*) href)
        :href href})
     "Groups"]]])

(defn header []
  [:header.my-5
   [breadcrumbs/main {:href (path :inventory-pool-entitlement-groups
                                  {:inventory-pool-id @inventory-pool/id*})}]
   [:h1.mt-3 [entitlement-group-name]]
   [:h6 "Inventory Pool " [inventory-pool/name-component]]])
