(ns leihs.admin.resources.users.user.delete
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [clojure.set :refer [rename-keys]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.users.user.core :as user-core :refer [user-id*]]
   [leihs.admin.state :as state]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent]))

(def transfer-data* (reagent/atom {}))

(defn set-transfer-data-by-query-params [& _]
  (reset! transfer-data*
          (-> @routing/state*
              :query-params-raw
              (select-keys [:user-uid])
              (rename-keys {:user-uid :target-user-uid}))))

(defn delete-user [& _]
  (go (when (some->
             {:url (path :user (-> @routing/state* :route-params))
              :method :delete
              :chan (async/chan)}
             http-client/request :chan <!
             http-client/filter-success!)
        (accountant/navigate! (path :users)))))

(defn transfer-data-and-delete-user [& _]
  (go (when (some->
             {:url  (path :user-transfer-data
                          {:user-id @user-id*
                           :target-user-uid (:target-user-uid @transfer-data*)})
              :method :delete
              :chan (async/chan)}
             http-client/request :chan <!
             http-client/filter-success!)
        (accountant/navigate! (path :users)))))

(defn delete-without-reasignment-component []
  [:<>
   [:p
    "Deleting this user is not possible if it is associated with contracts, reserverations, or orders. "
    "If this is the case this operation will fail without deleting or even changing any data. "]
   [:p.font-weight-bold.text-danger
    "Permissions, such as given by delegations, groups, or roles will not prevent deletion of this user. "]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn target-user-choose-component []
  [:div.input-group-append
   [:a.btn.btn-info
    {:tab-index 3
     :href (path :users-choose {}
                 {:return-to (path (:handler-key @routing/state*)
                                   (:route-params @routing/state*)
                                   @transfer-data*)})}
    [:i.fas.fa-rotate-90.fa-hand-pointer.px-2]
    " Choose user "]])

(defn delete-with-transfer-component []
  [:<>
   [routing/hidden-state-component
    {:did-mount set-transfer-data-by-query-params}]
   [:p.font-weight-bold.mt-5 "Transfer Data"]
   [:p
    "Contracts, reserverations, and orders of this user will be "
    "transferred to the user entered below. "
    "The audit trace is still intact and complete as this action is audited, too. "
    "However, it is much more involved to follow it."]

   [:ul.text-danger
    [:li
     "Associations to entitlements, delegations, groups, et cetera will be removed! "]
    [:li
     "Roles (permissions) to inventory pools will be removed! "]
    [:li
     "Audits will still contain references to the removed user! "]
    [:li
     "External data, such as open contracts printed on paper for example, will become inconsistent with the data in leihs!"]]
   [form-components/input-component transfer-data* [:target-user-uid]
    :label "Target user"
    :placeholder "email-address, login, or id, or choose by clicking the button"
    :append target-user-choose-component]])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div
     [:div.data
      [:h3 "transfer-data*"]
      [:pre (with-out-str (pprint @transfer-data*))]]
     [:div.data
      [:h3 "transfer-data*"]
      [:pre (with-out-str
              (pprint
               (path :user-transfer-data
                     {:user-id @user-id*
                      :target-user-uid (:target-user-uid @transfer-data*)})))]]]))

(defn dialog [& {:keys [show onHide]
                 :or {show false}}]
  [:> Modal {:size "lg"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Delete User"]]
   [:> Modal.Body
    [delete-without-reasignment-component]
    [delete-with-transfer-component]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick onHide}
     "Cancel"]
    (if (empty? (:target-user-uid @transfer-data*))
      [:> Button {:variant "danger"
                  :onClick #(delete-user)}
       "Delete"]
      [:> Button {:variant "danger"
                  :onClick #(transfer-data-and-delete-user)}
       "Transfer and delete"])]])
