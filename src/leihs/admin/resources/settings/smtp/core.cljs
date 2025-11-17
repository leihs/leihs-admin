(ns leihs.admin.resources.settings.smtp.core
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [react-bootstrap :as react-bootstrap :refer [Col Form Row]]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn fetch []
  (go (reset! data*
              (some-> {:chan (async/chan)}
                      http-client/request :chan <!
                      http-client/filter-success :body))))

(defn form [action data*]
  [:> Form
   {:id "smtp-form"
    :on-submit (fn [e]
                 (.preventDefault e)
                 (action))}
   [:div.mb-4
    [form-components/checkbox-component data* [:enabled]
     :label "Sending Emails Enabled"]]

   [:h4.mb-3 "SMTP Settings"]
   [:> Row
    [:> Col
     [form-components/input-component data* [:port]
      :type :number :label "Server port"]]
    [:> Col
     [form-components/input-component data* [:address]
      :label "Server address"]]]
   [:> Row
    [:> Col
     [form-components/input-component data* [:domain]
      :label "Domain name"]]
    [:> Col
     [form-components/input-component data* [:default_from_address]
      :label "From"]]
    [:> Col
     [form-components/input-component data* [:sender_address]
      :label "Sender"]]]

   [:> Row
    [:> Col
     [form-components/input-component data* [:username]
      :label "User"]]
    [:> Col
     [form-components/input-component data* [:password]
      :label "Password"]]]
   [:> Row
    [:> Col
     [form-components/input-component data* [:authentication_type]]]
    [:> Col
     [form-components/input-component data* [:openssl_verify_mode]]]
    [:> Col
     [form-components/checkbox-component data* [:enable_starttls_auto]]]]

   [:hr.my-4]
   [:h4.mb-3 "Microsoft 365 OAuth Settings"]

   [:div.my-3
    [form-components/checkbox-component data* [:ms365_enabled]
     :label "Microsoft 365 OAuth Enabled"]]

   [:> Row
    [:> Col
     [form-components/input-component data* [:ms365_client_id]
      :label "Client ID"]]
    [:> Col
     [form-components/input-component data* [:ms365_tenant_id]
      :label "Tenant ID"]]]

   [:> Row
    [:> Col
     [form-components/input-component data* [:ms365_client_secret]
      :type :password
      :label "Client Secret"]]]])
