(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.create
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(defn create []
  (go (when-let [id (some->
                     {:url (path :inventory-pool-pickup-locations
                                 {:inventory-pool-id @pool-core/id*})
                      :method :post
                      :json-params @data*
                      :chan (async/chan)}
                     http-client/request :chan <!
                     http-client/filter-success!
                     :body :id)]
        (accountant/navigate!
         (path :inventory-pool-pickup-location
               {:inventory-pool-id @pool-core/id*
                :pickup-location-id id})))))

(def open*
  (reaction
   (reset! data* nil)
   (->> (:query-params @routing/state*)
        :action
        (= "add"))))

(defn dialog []
  [:> Modal {:size "md"
             :centered true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Add Pickup Location"]]
   [:> Modal.Body
    [core/pickup-location-form data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:on-click #(create)}
     "Save"]]])

(defn button []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    [:<>
     [:> Button
      {:className "ml-3"
       :on-click #(search-params/append-to-url {:action "add"})}
      "Add Pickup Location"]]))
