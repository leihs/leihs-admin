(ns leihs.admin.resources.settings.misc.main
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.resources.settings.misc.core :as core]
   [leihs.admin.resources.settings.misc.edit :as edit]
   [leihs.admin.resources.settings.shared.components :refer [row]]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn info-table []
  [table/container
   {:borders false
    :header [:tr [:th "Property"] [:th.w-75 "Value"]]
    :body
    [:<>
     [row "Logo URL" :logo_url @core/data*]
     [row "Documentation Link" :documentation_link @core/data*]
     [row "Contract Lending Party" :contract_lending_party_string @core/data*]
     [row "Custom Head Tag" :custom_head_tag @core/data*]
     [row "Location" :time_zone @core/data*]
     [row "Currency" :local_currency_string @core/data*]
     [row "Borrow: Cart Timeout" :timeout_minutes @core/data*]
     [row "Email Signature" :email_signature @core/data*]
     [row "Include Customer Email in Contracts" :include_customer_email_in_contracts @core/data*]
     [row "Lending Terms Acceptance required for Order" :lending_terms_acceptance_required_for_order @core/data*]
     [row "Lending Terms URL" :lending_terms_url @core/data*]
     [row "Show Contact Details on Customer Order" :show_contact_details_on_customer_order @core/data*]
     [row "Home Page Image URL" :home_page_image_url @core/data*]]}])

(defn debug-component []
  (when @state/debug?*
    [:div.debug
     [:h3 "@misc-core/data*"]
     [:pre (with-out-str (pprint @core/data*))]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount core/fetch}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]
     [:article.settings-page
      [:header.my-5
       [:h1 [icons/list-icon] " Miscellaneous Settings"]]

      [:section.mb-5
       [info-table]
       [edit/button]
       [edit/dialog]]

      [debug-component]])])
