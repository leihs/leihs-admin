(ns leihs.admin.resources.inventory-pools.inventory-pool.edit
  (:require
   [cljs.core.async :as async]
   [clojure.core.async :refer [<! go]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [leihs.core.user.front :as current-user]
   [react-bootstrap :refer [Button Modal]]
   [reagent.core :refer [reaction]]))

(defn patch []
  (let [route (path :inventory-pool
                    {:inventory-pool-id @core/id*})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params @core/data*
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
          (search-params/delete-all-from-url)))))

(defn form [& {:keys [is-editing]
               :or {is-editing false}}]
  [:div.inventory-pool.mt-3
   [:div.mb-3
    [form-components/switch-component core/data* [:is_active]
     :disabled (not @current-user/admin?*)
     :label "Active"]]
   [:div
    [form-components/input-component core/data* [:name]
     :label "Name"
     :required true]]
   [:div
    [form-components/input-component core/data* [:shortname]
     :label "Short name"
     :disabled is-editing
     :required true]]
   [:div
    [form-components/input-component core/data* [:email]
     :label "Email"
     :type :email
     :required true]]
   [form-components/input-component core/data* [:description]
    :label "Description"
    :element :textarea
    :rows 10]
   [form-components/input-component core/data* [:default_contract_note]
    :label "Default Contract Note"
    :element :textarea
    :rows 5]
   [:div.mb-3
    [form-components/switch-component core/data* [:print_contracts]
     :label "Print Contracts"]]
   [:div.mb-3
    [form-components/switch-component core/data* [:automatic_suspension]
     :label "Automatic Suspension"]]
   (when (:automatic_suspension @core/data*)
     [form-components/input-component core/data* [:automatic_suspension_reason]
      :label "Automatic Suspension Reason"
      :element :textarea
      :rows 5])
   [:div.mb-3
    [form-components/switch-component core/data* [:required_purpose]
     :label "Hand Over Purpose"]]
   [:div.mb-3
    [form-components/input-component core/data* [:reservation_advance_days]
     :label "Reservation Advance Days"
     :type :number
     :min 0]]])

(def open*
  (reaction
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn dialog []
  [:> Modal {:size "lg"
             :centered true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :onHide #(search-params/delete-all-from-url)}
    [:> Modal.Title "Edit Inventory Pool"]]
   [:> Modal.Body
    [form {:is-editing true}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-all-from-url)}
     "Cancel"]
    [:> Button {:on-click #(patch)}
     "Save"]]])

(defn button []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    [:<>
     [:> Button
      {:className ""
       :on-click #(search-params/append-to-url {:action "edit"})}
      "Edit"]
     [dialog]]))
