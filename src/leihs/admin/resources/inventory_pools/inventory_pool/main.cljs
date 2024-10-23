(ns leihs.admin.resources.inventory-pools.inventory-pool.main
  (:require
   [clojure.string :refer [join]]
   [leihs.admin.common.components :refer [toggle-component]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.delete :as delete]
   [leihs.admin.resources.inventory-pools.inventory-pool.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as BS :refer [Button]]))

;;; components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn property-td
  ([label col-name] (property-td label col-name nil))
  ([label col-name hint]
   [:td [:strong label] [:small.text-monospace (str " (" col-name ")")]
    (when hint [:small.form-text hint])]))

(defn inventory-pool []
  [:div
   [table/container
    {:borders false
     :header [:tr [:th "Property"] [:th.w-75 "Value"]]
     :body
     [:<>
      [:tr.active
       [property-td "Active" "is_active"]
       [:td.active
        (toggle-component (:is_active @core/data*))]]
      [:tr.name
       [property-td "Name" "name"]
       [:td.name
        (:name @core/data*)]]
      [:tr.shortname
       [property-td "Short Name" "shortname"
        "Prefix for auto-generated inventory codes"]
       [:td.shortname (:shortname @core/data*)]]
      [:tr.email
       [property-td "Email" "email"
        "from_address for emails send in the name of this pool"]
       [:td.email (:email @core/data*)]]
      [:tr.description
       [property-td "Description" "description"
        "Visible for customers in the borrow app"]
       [:td.description
        [:div {:style {:white-space "break-spaces",
                       :overflow-y "auto"
                       :height "200px"}}
         (:description @core/data*)]]]
      [:tr.default-contract-note
       [property-td "Default Contract Note" "default_contract_note"]
       [:td.default-contract-note
        {:style {:white-space "break-spaces"}}
        (:default_contract_note @core/data*)]]
      [:tr.print-contracts
       [property-td "Print Contracts" "print_contracts"
        "Whether to open print dialog automatically upon hand over"]
       [:td.default-contract-note
        (toggle-component (:print_contracts @core/data*))]]
      [:tr.automatic-suspension
       [property-td "Automatic Suspension" "automatic_suspension"
        "Users who don't bring back the items on the required date are suspended from next day on."]
       [:td.automatic-suspension
        (toggle-component (:automatic_suspension @core/data*))]]
      (when (:automatic_suspension @core/data*)
        [:tr.automatic-suspension-reason
         [property-td "Automatic Suspension Reason" "automatic_suspension_reason"]
         [:td.automatic-suspension-reason
          {:style {:white-space "break-spaces"}}
          (:automatic_suspension_reason @core/data*)]])
      [:tr.required-purpose
       [property-td "Hand Over Purpose" "required_purpose"
        "Whether the specification of hand over purpose is required."]
       [:td.required-purpose
        (toggle-component (:required_purpose @core/data*))]]
      [:tr.deliver-received-order-emails
       [property-td "Deliver Received Order Emails" "deliver_received_order_emails"
        "Receive an email to the pool's address when an order for this pool is submitted."]
       [:td.deliver-received-order-emails
        (toggle-component (:deliver_received_order_emails @core/data*))]]
      [:tr.borrow-reservation-advance-days
       [property-td "Borrow: Reservation Advance Days" "borrow_reservation_advance_days"
        "Minimum number of days required between reservation's created date and the expected hand over date when placed by a customer in the borrow app."]
       [:td.borrow-reservation-advance-days
        (:borrow_reservation_advance_days @core/data*)]]
      [:tr.borrow-maximum-reservation-duration
       [property-td "Borrow: Maximum Reservation Duration" "borrow_maximum_reservation_duration"
        "Maximum duration in days allowed for a reservation when placed by a customer in the borrow app."]
       [:td.borrow-maximum-reservation-duration
        (:borrow_maximum_reservation_duration @core/data*)]]]}]])

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount #(core/fetch)}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]

     [:article.inventory-pool.my-5
      [core/header]

      [:section.info.mb-5
       [core/tabs (join ["/admin/inventory-pools/" @core/id*])]
       [inventory-pool]
       [edit/button]
       [edit/dialog]
       [delete/button]
       [delete/dialog]]

      [core/debug-component]])])
