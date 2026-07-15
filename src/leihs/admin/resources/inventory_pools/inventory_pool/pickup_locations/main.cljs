(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.main
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location-settings-edit :as settings-edit]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.create :as create]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Alert]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(def fetch-route*
  (reaction
   (path :inventory-pool-pickup-locations {:inventory-pool-id @pool-core/id*})))

(defn fetch []
  (http-client/route-cached-fetch data* {:route @fetch-route*}))

(defn property-td
  ([label col-name] (property-td label col-name nil))
  ([label col-name hint]
   [:td [:strong label] [:small.text-monospace (str " (" col-name ")")]
    (when hint [:small.form-text hint])]))

(defn settings-table []
  [table/container
   {:borders false
    :header [:tr [:th.w-50 "Property"] [:th.w-50 "Value"]]
    :body
    [:<>
     [:tr.default-pickup-location-name
      [property-td "Default Pickup Location Name" "default_pickup_location_name"
       "Shown as the pickup location placeholder in the borrow area when no alternative pickup location is chosen."]
      [:td.default-pickup-location-name (:default_pickup_location_name @pool-core/data*)]]
     [:tr.transfer-buffer-before-pick-up
      [property-td "Transfer Buffer Before Pick-up" "transfer_buffer_before_pick_up"
       "Number of days needed to transfer an item to the pickup location before it can be picked up. Defaults to 0 if empty."]
      [:td.transfer-buffer-before-pick-up (:transfer_buffer_before_pick_up @pool-core/data*)]]
     [:tr.transfer-buffer-after-drop-off
      [property-td "Transfer Buffer After Drop-off" "transfer_buffer_after_drop_off"
       "Number of days needed to transfer an item back to the main warehouse after drop-off. Defaults to 0 if empty."]
      [:td.transfer-buffer-after-drop-off (:transfer_buffer_after_drop_off @pool-core/data*)]]]}])

(defn link-to-pickup-location [pickup-location inner]
  [:a {:href (path :inventory-pool-pickup-location
                   {:inventory-pool-id @pool-core/id*
                    :pickup-location-id (:id pickup-location)})}
   inner])

(defn pickup-locations-table [pickup-locations]
  (if-let [pickup-locations (seq pickup-locations)]
    [table/container
     {:className "pickup-locations"
      :header [:tr [:th "Index"] [:th "Name"] [:th "Description"]]
      :body
      (doall (for [[index pickup-location] (map-indexed vector pickup-locations)]
               ^{:key (:id pickup-location)}
               [:tr.pickup-location
                [:td (inc index)]
                [:td [link-to-pickup-location pickup-location (:name pickup-location)]]
                [:td (:description pickup-location)]]))}]
    [:> Alert {:variant "info"
               :className "text-center"}
     "No pickup locations found."]))

(defn pickup-locations-section []
  [:<>
   [table/toolbar [create/button]]
   (if-not (contains? @data* @fetch-route*)
     [wait-component]
     [pickup-locations-table (-> @data* (get @fetch-route*) :pickup-locations)])])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:section.debug
     [:hr]
     [:div
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount (fn []
                  (pool-core/fetch)
                  (fetch))}]

   (if-not @pool-core/data*
     [:div.my-5
      [wait-component]]

     [:article.pickup-locations
      [pool-core/header]

      [:section.mb-5
       [pool-core/tabs]
       [:> Alert {:variant "info"}
        "Pickup locations do not have their own opening times. The same opening times settings"
        " (workdays, holidays, etc.) apply to all pickup locations as for the inventory pool."]
       [settings-table]
       [settings-edit/button]
       [settings-edit/dialog]]

      [:section.mb-5
       [pickup-locations-section]
       [create/dialog]]

      [debug-component]])])
