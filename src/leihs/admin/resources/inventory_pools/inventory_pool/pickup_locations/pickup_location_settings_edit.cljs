(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location-settings-edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(defn patch []
  (let [route (path :inventory-pool {:inventory-pool-id @pool-core/id*})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params @data*
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
          (swap! pool-core/cache* assoc @pool-core/path* @data*)
          (search-params/delete-from-url "action")))))

(defn form []
  [:div
   [form-components/input-component data* [:default_pickup_location_name]
    :label "Default Pickup Location Name"
    :hint "Shown as the pickup location placeholder in the borrow area when no alternative pickup location is chosen."]
   [form-components/input-component data* [:transfer_buffer_before_pick_up]
    :label "Transfer Buffer Before Pick-up (days)"
    :type :number
    :min 0
    :placeholder "0"]
   [form-components/input-component data* [:transfer_buffer_after_drop_off]
    :label "Transfer Buffer After Drop-off (days)"
    :type :number
    :min 0
    :placeholder "0"]])

(def open*
  (reaction
   (reset! data* @pool-core/data*)
   (->> (:query-params @routing/state*)
        :action
        (= "edit-pickup-location-settings"))))

(defn dialog []
  [:> Modal {:size "md"
             :centered true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Edit Pickup Location Settings"]]
   [:> Modal.Body
    [form]]
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
      {:on-click #(search-params/append-to-url
                   {:action "edit-pickup-location-settings"})}
      "Edit"]]))
