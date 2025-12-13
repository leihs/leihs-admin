(ns leihs.admin.resources.settings.smtp.ms365-mailboxes.add
  (:require
   [clojure.string :as string]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.resources.settings.smtp.core :as smtp-core]
   [leihs.admin.state :as state]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal Alert]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce form-data* (reagent/atom {:email ""}))
(defonce error* (reagent/atom nil))

(defn url-encode [s]
  (js/encodeURIComponent s))

(defn build-oauth-url [email]
  "Construct MS365 OAuth authorization URL"
  (let [smtp-settings @smtp-core/data*
        tenant-id (:ms365_tenant_id smtp-settings)
        client-id (:ms365_client_id smtp-settings)
        external-base-url (-> @state/global-state*
                              :server-state
                              :settings
                              :external_base_url)
        redirect-uri (str external-base-url (path :smtp-ms365-callback))
        scope "https://graph.microsoft.com/Mail.Send offline_access"
        ;; Create state with email (CSRF token could be added here)
        state-json (.stringify js/JSON (clj->js {:email email}))
        state-param (url-encode state-json)]

    (str "https://login.microsoftonline.com/" tenant-id "/oauth2/v2.0/authorize"
         "?client_id=" (url-encode client-id)
         "&response_type=code"
         "&redirect_uri=" (url-encode redirect-uri)
         "&scope=" (url-encode scope)
         "&state=" state-param
         "&prompt=consent")))

(defn validate-and-redirect []
  (reset! error* nil)
  (let [email (string/trim (:email @form-data*))]
    (cond
      (empty? email)
      (reset! error* "Email address is required")

      (not (re-matches #"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$" email))
      (reset! error* "Please enter a valid email address")

      (not (-> @smtp-core/data* :ms365_client_id))
      (reset! error* "MS365 OAuth is not configured. Please configure Client ID and Tenant ID in Settings tab.")

      :else
      (try
        (let [oauth-url (build-oauth-url email)]
          (set! (.-location js/window) oauth-url))
        (catch js/Error e
          (reset! error* (str "Error building OAuth URL: " (.-message e))))))))

(def open?*
  (reaction
   (when (->> (:query-params @routing/state*)
              :action
              (= "add-mailbox"))
     (reset! form-data* {:email ""})
     (reset! error* nil)
     true)))

(defn dialog []
  [:> Modal {:size "md"
             :centered true
             :show @open?*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Add MS365 Mailbox"]]
   [:> Modal.Body
    [:form {:id "add-mailbox-form"
            :on-submit (fn [e]
                         (.preventDefault e)
                         (validate-and-redirect))}

     [:div.mb-3
      [:p "Enter the email address of the Microsoft 365 mailbox you want to add. "
       "You will be redirected to Microsoft to authorize access."]]

     [form-components/input-component form-data* [:email]
      :label "Email Address"
      :type :email
      :required true
      :placeholder "pool@example.com"
      :hint "This should match an inventory pool's email address"]

     (when @error*
       [:> Alert {:variant "danger"}
        @error*])]]

   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-mailbox-form"
                :variant "primary"}
     "Authorize with Microsoft"]]])

(defn button []
  [:> Button
   {:variant "primary"
    :on-click #(search-params/append-to-url {:action "add-mailbox"})}
   "+ Add Mailbox"])
