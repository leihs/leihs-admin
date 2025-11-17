(ns leihs.admin.resources.settings.smtp.main
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.resources.settings.shared.components :refer [row]]
   [leihs.admin.resources.settings.smtp.core :as core]
   [leihs.admin.resources.settings.smtp.edit :as edit]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn enabled-section []
  [:div.mb-4
   [table/container
    {:borders false
     :header [:tr [:th "Property"] [:th.w-75 "Value"]]
     :body
     [:<>
      [row "Sending Emails Enabled" :enabled @core/data*]]}]])

(defn smtp-table []
  [:<>
   [:h4.mb-3 "SMTP Settings"]
   [table/container
    {:borders false
     :header [:tr [:th "Property"] [:th.w-75 "Value"]]
     :body
     [:<>
      [row "Server Port" :port @core/data*]
      [row "Server Address" :address @core/data*]
      [row "Domain Name" :domain @core/data*]
      [row "From" :default_from_address @core/data*]
      [row "Sender Address" :sender_address @core/data*]
      [row "User Name" :username @core/data*]
      [:tr.password
       [:td [:strong "Password"] [:small " (password)"]]
       [:td.password {:style {:filter "blur(7px)"}} (str (:password @core/data*))]]
      [row "Authentication Type" :authentication_type @core/data*]
      [row "OpenSSL Verify Mode" :openssl_verify_mode @core/data*]
      [row "Enable Starttls Auto" :enable_starttls_auto @core/data*]]}]])

(defn ms365-table []
  [:<>
   [:h4.mb-3.mt-4 "Microsoft 365 OAuth Settings"]
   [table/container
    {:borders false
     :header [:tr [:th "Property"] [:th.w-75 "Value"]]
     :body
     [:<>
      [row "MS365 OAuth Enabled" :ms365_enabled @core/data*]
      [row "MS365 Client ID" :ms365_client_id @core/data*]
      [row "MS365 Tenant ID" :ms365_tenant_id @core/data*]
      [:tr.ms365_client_secret
       [:td [:strong "MS365 Client Secret"] [:small " (ms365_client_secret)"]]
       [:td.ms365_client_secret {:style {:filter "blur(7px)"}} (str (:ms365_client_secret @core/data*))]]]}]])

(defn debug-component []
  (when @state/debug?*
    [:div.debug
     [:h3 "@data*"]
     [:pre (with-out-str (pprint @core/data*))]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount #(core/fetch)}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]
     [:article.settings-page.smtp
      [:header.my-5
       [:h1 [icons/paper-plane] " Email Settings"]]

      [:section.mb-5
       [enabled-section]
       [smtp-table]
       [ms365-table]
       [edit/button]
       [edit/dialog]]

      [debug-component]])])
