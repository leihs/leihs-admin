(ns leihs.admin.resources.inventory-pools.inventory-pool.edit
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async]
   [clojure.core.async :refer [<! go]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as core]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.user.front :as current-user]
   [react-bootstrap :refer [Button Modal]]))

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
          (core/clean-and-fetch)
          (accountant/navigate! route)))))

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

(defn dialog [& {:keys [show onHide] :or {show false}}]
  [:> Modal {:size "lg"
             :centered true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Edit Inventory Pool"]]
   [:> Modal.Body
    [form {:is-editing true}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick #(do
                            (core/clean-and-fetch)
                            (onHide))}
     "Cancel"]
    [:> Button {:onClick #(patch)}
     "Save"]]])
