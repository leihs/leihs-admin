(ns leihs.admin.resources.inventory-pools.inventory-pool.users.user.suspension.main
  (:require ["date-fns" :as date-fns]
            [cljs.core.async :as async :refer [<! go]]
            [cljs.pprint :refer [pprint]]
            [leihs.admin.common.form-components :as form-components]
            [leihs.admin.common.http-client.core :as http-client]
            [leihs.admin.common.icons :as icons]
            [leihs.admin.paths :as paths :refer [path]]
            [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
            [leihs.admin.resources.inventory-pools.inventory-pool.suspension.core :as core]
            [leihs.admin.resources.inventory-pools.inventory-pool.users.user.breadcrumbs :as breadcrumbs]
            [leihs.admin.resources.users.user.core :as user]
            [leihs.admin.state :as state]
            [leihs.admin.utils.misc :refer [humanize-datetime-component
                                            wait-component]]
            [leihs.core.core :refer [presence]]
            [leihs.core.routing.front :as routing]
            [reagent.core :as reagent :refer [reaction]]))

(defn suspended? [suspended-until ref-date]
  (if-not suspended-until
    false
    (if (date-fns/isBefore suspended-until (date-fns/startOfDay ref-date))
      false
      true)))

(defn humanized-suspended-until-component [suspended-until]
  [:span
   (if-not (suspended? suspended-until (:timestamp @state/global-state*))
     [:span.text-success "Not suspended."]
     [:span.text-danger
      (if (date-fns/isAfter suspended-until (js/Date. "2098-01-01"))
        "Suspended forever."
        [:span "Suspended for "
         [humanize-datetime-component suspended-until :add-suffix false] "."])])])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn supension-inner-form-component [disabled data*]
  [:div.my-3
   [form-components/input-component data* [:suspended_until]
    :type :date
    :disabled disabled
    :label "Suspended until"]
   [form-components/input-component data* [:suspended_reason]
    :element :textarea
    :rows 3
    :disabled disabled
    :label "Reason"]])

(defn suspension-submit-component [supension edit-data* edit-mode?*]
  (let [changed* (reaction (not= supension @edit-data*))]
    [:div.row.mt-1
     [:div.col
      (if @changed*
        [:button.btn.btn-secondary
         {:type :button
          :on-click #(reset! edit-mode?* false)}
         [icons/delete] " Cancel"]
        [:button.btn.btn-outline-secondary
         {:type :button
          :on-click #(reset! edit-mode?* false)}
         [icons/delete] " Close"])]
     [:div.col
      [form-components/save-submit-component
       :disabled (not @changed*)]]]))

(defn suspension-edit-header []
  [:h4 "Edit Suspension"])

(defn suspension-edit-form-elements [edit-data*]
  [:div
   [:div.row
    [:div.col
     [:div.float-right
      [:button.btn.btn-secondary
       {:type :button
        :on-click #(reset! edit-data* {})}
       [:span [icons/delete] " Reset suspension"]]]]]
   [supension-inner-form-component false edit-data*]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce data* (reagent/atom nil))

(defn user-page-suspension-component []
  [:div
   (let [suspension-path (path :inventory-pool-user-suspension
                               (some-> @routing/state* :route-params))]
     [:<>
      [routing/hidden-state-component
       {:did-mount
        #(go (reset! data* (<! (core/fetch-suspension< suspension-path))))}]
      [core/suspension-component @data*
       :update-handler
       #(go (reset! data* (<! (core/put-suspension< suspension-path %))))]])])

(defn header-component []
  [:h1 "Suspension of "
   [user/name-link-component]
   " in "
   [inventory-pool/name-link-component]])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:section.debug
     [:hr]
     [:h2 "Page Debug"]
     [:div
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))

(defn page []
  [:div.inventory-pool-user-suspension
   [breadcrumbs/nav-component
    (conj @breadcrumbs/left* [breadcrumbs/suspension-li]) []]
   [header-component]
   [user-page-suspension-component]
   [debug-component]])
