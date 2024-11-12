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

(defn info-table []
  [table/container
   {:borders false
    :header [:tr [:th "Property"] [:th.w-75 "Value"]]
    :body
    [:<>
     [row "Sending Emails Enabled" :enabled @core/data*]
     [row "Server Port" :port @core/data*]
     [row "Server Address" :address @core/data*]
     [row "Domain Name" :domain @core/data*]
     [row "From" :default_from_address @core/data*]
     [row "Sender Address" :sender_address @core/data*]
     [row "User Name" :username @core/data*]
     [row "Password" :password @core/data*]
     [row "Authentication Type" :authentication_type @core/data*]
     [row "OpenSSL Verify Mode" :openssl_verify_mode @core/data*]
     [row "Enable Starttls Auto" :enable_starttls_auto @core/data*]]}])

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
       [:h1 [icons/paper-plane] " SMTP Settings"]]

      [:section.mb-5
       [info-table]
       [edit/button]
       [edit/dialog]]

      [debug-component]])])
