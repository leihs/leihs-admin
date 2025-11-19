(ns leihs.admin.resources.settings.smtp.main
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<!]]
   [cljs.pprint :refer [pprint]]
   [clojure.contrib.inflect :refer [pluralize-noun]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.resources.settings.shared.components :refer [row]]
   [leihs.admin.resources.settings.smtp.core :as core]
   [leihs.admin.resources.settings.smtp.edit :as edit]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Form Tab Tabs]]
   [reagent.core :as reagent]))

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

(defn settings-tab []
  [:div.mt-4
   [enabled-section]
   [smtp-table]
   [ms365-table]
   [:div.mb-3 [edit/button]]
   [edit/dialog]])

(defonce test-email-data* (reagent/atom {:from_address ""
                                                      :to_address ""
                                                      :subject "Test Email"
                                                      :body "This is a test email."}))

(defonce test-email-result* (reagent/atom nil))

(defn send-test-email []
  (async/go
    (reset! test-email-result* {:status :sending})
    (let [response (<! (:chan (http-client/request
                                {:method :post
                                 :url (path :smtp-test-email)
                                 :json-params @test-email-data*
                                 :chan (async/chan)})))]
      (if (:success response)
        (reset! test-email-result* {:status :success
                                    :message "Test email enqueued for sending. Refresh this page to see its status."})
        (reset! test-email-result* {:status :error
                                    :message (or (-> response :body :message)
                                                 "Failed to send test email")})))))

(defn test-form []
  [:div.mb-4
   [:h4.mb-3 "Send Test Email"]
   [:> Form {:on-submit (fn [e]
                          (.preventDefault e)
                          (send-test-email))}
    [form-components/input-component test-email-data* [:from_address]
     :label "From Address" :required true]
    [form-components/input-component test-email-data* [:to_address]
     :label "To Address" :required true]
    [form-components/input-component test-email-data* [:subject]
     :label "Subject" :required true]
    [form-components/input-component test-email-data* [:body]
     :element :textarea :rows 4
     :label "Message" :required true]

    (when @test-email-result*
      (case (:status @test-email-result*)
        :sending [:div.alert.alert-info "Sending..."]
        :success [:div.alert.alert-success (:message @test-email-result*)]
        :error [:div.alert.alert-danger (:message @test-email-result*)]
        nil))

    [:div.mb-3
     [:> Button {:type "submit" :variant "primary"}
      "Send Test Email"]]]])

(defonce emails-data* (reagent/atom nil))
(defonce current-page* (reagent/atom 0))

(def page-size 10)

(defn fetch-emails []
  (async/go
    (let [offset (* @current-page* page-size)
          response (<! (:chan (http-client/request
                                {:method :get
                                 :url (str (path :smtp-emails) "?limit=" page-size "&offset=" offset)
                                 :chan (async/chan)})))]
      (when (:success response)
        (reset! emails-data* (:body response))))))

(defn emails-table []
  (if-not @emails-data*
    [:div [wait-component]]
    [table/container
     {:borders true
      :header [:tr
               [:th "From"]
               [:th "To"]
               [:th "Subject"]
               [:th "Status"]
               [:th "Attempts"]
               [:th "Message"]
               [:th "Created"]]
      :body
      (if (empty? (:emails @emails-data*))
        [:<> [:tr [:td {:col-span 7 :class "text-center"} "No emails found"]]]
        [:<>
         (for [email (:emails @emails-data*)]
           [:tr {:key (:id email)}
            [:td (:from_address email)]
            [:td (:to_address email)]
            [:td (:subject email)]
            [:td (if (= 0 (:code email))
                   [:span.badge.bg-success "Sent"]
                   [:span.badge.bg-danger "Failed"])]
            [:td (:trials email)]
            [:td (:message email)]
            [:td (str (:created_at email))]])])}]))

(defn pagination-controls []
  (let [total-count (or (:total @emails-data*) 0)
        total-pages (js/Math.ceil (/ total-count page-size))
        current-page @current-page*]
    (when (> total-pages 1)
      [:div.d-flex.justify-content-between.align-items-center.mt-3.mb-3
       [:div
        [:> Button {:variant "secondary"
                    :disabled (= current-page 0)
                    :on-click #(do (swap! current-page* dec)
                                   (fetch-emails))}
         "Previous"]]
       [:div.text-center
        [:span "Page " (inc current-page) " of " total-pages
         " (" total-count " total emails)"]]
       [:div
        [:> Button {:variant "secondary"
                    :disabled (>= current-page (dec total-pages))
                    :on-click #(do (swap! current-page* inc)
                                   (fetch-emails))}
         "Next"]]])))

(defn emails-list []
  [:div
   [:h4.mb-3 "Email History"]
   [:div.alert.alert-info
    "Emails are processed asynchronously with automatic retry attempts. Refresh this page to see updated status."]
   [emails-table]
   [pagination-controls]])

(defn test-history-tab []
  [:div.mt-4
   [test-form]
   [:hr.my-4]
   [emails-list]])

(defn debug-component []
  (when @state/debug?*
    [:div.debug
     [:h3 "@data*"]
     [:pre (with-out-str (pprint @core/data*))]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount (fn []
                  (core/fetch)
                  (when (= (-> @routing/state* :query-params-raw :tab) "test-history")
                    (fetch-emails)))}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]
     [:article.settings-page.smtp
      [:header.my-5
       [:h1 [icons/paper-plane] " Email Settings"]]

      (let [active-tab (or (-> @routing/state* :query-params-raw :tab)
                           "settings")]
        [:> Tabs {:key active-tab
                  :className "mt-4"
                  :activeKey active-tab
                  :transition false
                  :onSelect (fn [key]
                              (accountant/navigate! (str (path :smtp-settings) "?tab=" key))
                              (when (= key "test-history")
                                (fetch-emails)))}
         [:> Tab {:eventKey "settings" :title "Settings"}
          [settings-tab]]
         [:> Tab {:eventKey "test-history" :title "Test & History"}
          [test-history-tab]]])

      [debug-component]])])
