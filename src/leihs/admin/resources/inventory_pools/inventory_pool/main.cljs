(ns leihs.admin.resources.inventory-pools.inventory-pool.main
  (:require
   [clojure.string :refer [join]]
   [goog.string :as string]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.delete :as delete]
   [leihs.admin.resources.inventory-pools.inventory-pool.edit :as edit]
   [leihs.admin.resources.inventory-pools.inventory-pool.nav :as nav]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.main :as workdays]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as BS :refer [Button]]
   [reagent.core :as reagent]))

;;; components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn edit-inventory-pool []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    (let [show (reagent/atom false)]
      (fn []
        [:<>
         [:> Button
          {:className ""
           :onClick #(reset! show true)}
          "Edit"]
         [edit/dialog {:show @show
                       :onHide #(reset! show false)}]]))))

(defn delete-inventory-pool []
  (when (auth/allowed? [auth/admin-scopes?])
    (let [show (reagent/atom false)]
      (fn []
        [:<>
         [:> Button
          {:className "ml-3"
           :variant "danger"
           :onClick #(reset! show true)}
          "Delete"]
         [delete/dialog {:show @show
                         :onHide #(reset! show false)}]]))))

(defn property-td
  ([label col-name] (property-td label col-name nil))
  ([label col-name hint]
   [:td [:strong label] [:small.text-monospace (str " (" col-name ")")]
    (when hint [:small.form-text hint])]))

(defn inventory-pool []
  (if-not @inventory-pool/data*
    [wait-component]
    [:div
     [table/container
      {:borders false
       :header [:tr [:th "Property"] [:th.w-75 "Value"]]
       :body
       [:<>
        [:tr.active
         [property-td "Active" "is_active"]
         [:td.active
          [:div.custom-control.custom-switch
           [:input.custom-control-input {:id "active-switch"
                                         :type "checkbox",
                                         :disabled true,
                                         :checked (:is_active @inventory-pool/data*)}]
           [:label.custom-control-label {:for "active-switch"}]]]]
        [:tr.name
         [property-td "Name" "name"]
         [:td.name
          (:name @inventory-pool/data*)]]
        [:tr.shortname
         [property-td "Short Name" "shortname"
          "Prefix for auto-generated inventory codes"]
         [:td.shortname (:shortname @inventory-pool/data*)]]
        [:tr.email
         [property-td "Email" "email"
          "from_address for emails send in the name of this pool"]
         [:td.email (:email @inventory-pool/data*)]]
        [:tr.description
         [property-td "Description" "description"
          "Visible for customers in the borrow app"]
         [:td.description
          [:div {:style {:white-space "break-spaces",
                         :overflow-y "auto"
                         :height "200px"}}
           (:description @inventory-pool/data*)]]]
        [:tr.default-contract-note
         [property-td "Default Contract Note" "default_contract_note"]
         [:td.default-contract-note
          {:style {:white-space "break-spaces"}}
          (:default_contract_note @inventory-pool/data*)]]
        [:tr.print-contracts
         [property-td "Print Contracts" "print_contracts"
          "Whether to open print dialog automatically upon hand over"]
         [:td.default-contract-note
          [:div.custom-control.custom-switch
           [:input.custom-control-input {:id "print-contracts-switch"
                                         :type "checkbox",
                                         :disabled true,
                                         :checked (:print_contracts @inventory-pool/data*)}]
           [:label.custom-control-label {:for "print-contracts-switch"}]]]]
        [:tr.automatic-suspension
         [property-td "Automatic Suspension" "automatic_suspension"
          "Users who don't bring back the items on the required date are suspended from next day on."]
         [:td.automatic-suspension
          [:div.custom-control.custom-switch
           [:input.custom-control-input {:id "automatic-suspension-switch"
                                         :type "checkbox",
                                         :disabled true,
                                         :checked (:automatic_suspension @inventory-pool/data*)}]
           [:label.custom-control-label {:for "automatic-suspension-switch"}]]]]
        [:tr.automatic-suspension-reason
         [property-td "Automatic Suspension Reason" "automatic_suspension_reason"]
         [:td.automatic-suspension-reason
          {:style {:white-space "break-spaces"}}
          (:automatic_suspension_reason @inventory-pool/data*)]]
        [:tr.required-purpose
         [property-td "Hand Over Purpose" "required_purpose"
          "Whether the specification of hand over purpose is required."]
         [:td.required-purpose
          [:div.custom-control.custom-switch
           [:input.custom-control-input {:id "required-purpose"
                                         :type "checkbox",
                                         :disabled true,
                                         :checked (:required_purpose @inventory-pool/data*)}]
           [:label.custom-control-label {:for "required-purpose"}]]]]]}]
     [edit-inventory-pool]
     [delete-inventory-pool]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount inventory-pool/clean-and-fetch}]
   (if-not @inventory-pool/data*
     [:div.my-5
      [wait-component " Loading Data ..."]]
     [:article.inventory-pool.my-5
      [:h1.my-5
       [inventory-pool/name-component]]
      [nav/tabs (join ["/admin/inventory-pools/" @inventory-pool/id*])]
      [inventory-pool]
      [workdays/component]
      [inventory-pool/debug-component]])])
