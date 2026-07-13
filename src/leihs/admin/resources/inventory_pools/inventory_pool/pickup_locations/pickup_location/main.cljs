(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.main
  (:require
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.delete :as delete]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn info-table []
  [table/container
   {:borders false
    :header [:tr [:th "Property"] [:th.w-75 "Value"]]
    :body
    [:<>
     [:tr.name
      [:td [:strong "Name"]]
      [:td.name (:name @core/data*)]]
     [:tr.description
      [:td [:strong "Description"]]
      [:td.description
       {:style {:white-space "break-spaces"}}
       (:description @core/data*)]]
     [:tr.created-at
      [:td [:strong "Created"]]
      [:td.created-at (:created_at @core/data*)]]]}])

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount (fn []
                  (pool-core/fetch)
                  (core/fetch))}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]

     [:article.pickup-location
      [pool-core/header]

      [:section.mb-5
       [pool-core/tabs]
       [info-table]
       [edit/button]
       [edit/dialog]
       [delete/button]
       [delete/dialog]]

      [core/debug-component]])])
