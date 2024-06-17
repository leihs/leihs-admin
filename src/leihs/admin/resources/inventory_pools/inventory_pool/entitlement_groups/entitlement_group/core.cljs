(ns leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.core
  (:require [cljs.core.async :as async :refer [<! go]]
            [leihs.admin.common.components.navigation.breadcrumbs :as breadcrumbs]
            [leihs.admin.common.http-client.core :as http-client]
            [leihs.admin.paths :as paths :refer [path]]
            [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
            [leihs.core.core :refer [presence]]
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
  (go (reset! data*
              (some->
               {:url @path*
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success! :body))))

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
   [breadcrumbs/main]
   [:h1.mt-3 (:name @data*)]
   [:h6 "Inventory Pool " (:name @inventory-pool/data*)]])
