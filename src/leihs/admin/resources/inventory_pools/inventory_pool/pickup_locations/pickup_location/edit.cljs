(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(defn patch []
  (go (when (some->
             {:url @core/path*
              :method :patch
              :json-params @data*
              :chan (async/chan)}
             http-client/request :chan <!
             http-client/filter-success!)
        (swap! core/cache* assoc @core/path* @data*)
        (search-params/delete-from-url "action"))))

(def open*
  (reaction
   (reset! data* @core/data*)
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn dialog []
  [:> Modal {:size "md"
             :centered true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Edit Pickup Location"]]
   [:> Modal.Body
    [core/pickup-location-form data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:on-click #(patch)}
     "Save"]]])

(defn button []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    [:<>
     [:> Button
      {:on-click #(search-params/append-to-url {:action "edit"})}
      "Edit"]]))
