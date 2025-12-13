(ns leihs.admin.resources.settings.smtp.ms365-mailboxes.main
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.resources.settings.smtp.ms365-mailboxes.add :as add]
   [leihs.admin.utils.misc :refer [wait-component]]
   [react-bootstrap :as react-bootstrap :refer [Button Badge]]
   [reagent.core :as reagent]))

(defonce mailboxes-data* (reagent/atom nil))

(defn fetch-mailboxes []
  (go (let [response (<! (:chan (http-client/request
                                  {:method :get
                                   :url (path :smtp-ms365-mailboxes)
                                   :chan (async/chan)})))]
        (when (:success response)
          (reset! mailboxes-data* (:body response))))))

(defn calculate-status [mailbox]
  "Calculate mailbox status based on token expiry"
  (let [now (js/Date.)
        expires-at (when (:token_expires_at mailbox)
                     (js/Date. (:token_expires_at mailbox)))
        five-minutes (* 5 60 1000)]
    (cond
      (nil? expires-at) :error
      (> (.getTime expires-at) (+ (.getTime now) five-minutes)) :connected
      (> (.getTime expires-at) (.getTime now)) :expiring
      :else :expired)))

(defn status-badge [mailbox]
  (case (calculate-status mailbox)
    :connected [:> Badge {:bg "success"} "Connected"]
    :expiring [:> Badge {:bg "warning"} "Expiring Soon"]
    :expired [:> Badge {:bg "danger"} "Expired"]
    :error [:> Badge {:bg "secondary"} "Error"]
    [:> Badge {:bg "secondary"} "Unknown"]))

(defn format-timestamp [ts]
  (if ts
    (-> (js/Date. ts)
        (.toLocaleString))
    "N/A"))

(defn delete-mailbox [mailbox-id]
  (when (js/confirm (str "Are you sure you want to delete mailbox '" mailbox-id "'?"))
    (go (let [response (<! (:chan (http-client/request
                                    {:method :delete
                                     :url (path :smtp-ms365-mailbox
                                               {:mailbox-id mailbox-id})
                                     :chan (async/chan)})))]
          (when (:success response)
            (fetch-mailboxes))))))

(defn mailboxes-table []
  (if-not @mailboxes-data*
    [:div [wait-component]]
    [table/container
     {:borders true
      :header [:tr
               [:th "Email Address"]
               [:th "Status"]
               [:th "Token Expires"]
               [:th "Created"]
               [:th "Actions"]]
      :body
      (if (empty? (:mailboxes @mailboxes-data*))
        [:<> [:tr [:td {:col-span 5 :class "text-center"}
                   "No mailboxes configured. Click 'Add Mailbox' to get started."]]]
        [:<>
         (for [mailbox (:mailboxes @mailboxes-data*)]
           [:tr {:key (:id mailbox)}
            [:td [:strong (:id mailbox)]]
            [:td [status-badge mailbox]]
            [:td (format-timestamp (:token_expires_at mailbox))]
            [:td (format-timestamp (:created_at mailbox))]
            [:td
             [:> Button {:variant "danger"
                        :size "sm"
                        :on-click #(delete-mailbox (:id mailbox))}
              [icons/delete] " Delete"]]])])}]))

(defn page []
  [:div.mt-4
   [:div.d-flex.justify-content-between.align-items-center.mb-3
    [:h4 "MS365 Mailboxes"]
    [add/button]]

   [:div.alert.alert-info
    [:strong "Matching Logic: "]
    "When sending emails, the system will automatically use the MS365 mailbox "
    "whose email address matches the inventory pool's email field."
    [:br] [:br]
    [:strong "Token Expiration: "]
    "Expired tokens are automatically refreshed when sending emails."]

   [mailboxes-table]
   [add/dialog]])
