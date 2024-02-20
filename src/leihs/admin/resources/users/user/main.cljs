(ns leihs.admin.resources.users.user.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.admin.common.components.navigation.back :as back]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.users.user.core :as user-core :refer [clean-and-fetch user-data*
                                                                user-id*]]
   [leihs.admin.resources.users.user.delete :as delete]
   [leihs.admin.resources.users.user.edit :as edit]
   [leihs.admin.resources.users.user.groups :as groups]
   [leihs.admin.resources.users.user.inventory-pools :as inventory-pools]
   [leihs.admin.resources.users.user.password-reset.main :as password-reset]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button ButtonGroup DropdownButton Dropdown]]
   [reagent.core :as reagent]))

;; (defn breadcrumbs []
;;   [breadcrumbs/nav-component
;;    @breadcrumbs/left*
;;    [[audited-changes-breadcrumbs/changes-li
;;      :query-params {:pkey (:id @user-data*)
;;                     :table "users"}]
;;     [breadcrumbs-common/email-li (:email @user-data*)]
;;     [breadcrumbs/user-password-reset-li @user-id*]
;;     [breadcrumbs/delete-li]
;;     [breadcrumbs/edit-li]
;;     [breadcrumbs/user-my-li @user-id*]]])

(defn edit-user-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:onClick #(reset! show true)}
        "Edit User"]
       [edit/dialog {:show @show
                     :onHide #(reset! show false)}]])))

(defn reset-password-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> DropdownButton {:as ButtonGroup :title "Reset Password"}
        [:> Dropdown.Item {:on-click #(do (reset! show true)
                                          (password-reset/submit :valid_for 1))}
         "Create Reset Link - 1 hour"]
        [:> Dropdown.Item {:on-click #(do (reset! show true)
                                          (password-reset/submit))}
         "Create Reset Link - 24 hours"]
        [:> Dropdown.Item {:on-click #(do (reset! show true)
                                          (password-reset/submit :valid_for (* 3 24)))}
         "Create Reset Link - 3 days"]
        [:> Dropdown.Item {:on-click #(do (reset! show true)
                                          (password-reset/submit {:valid_for (* 7 24)}))}
         "Create Reset Link - 7 days"]]
       [password-reset/dialog  {:show @show
                                :onHide #(reset! show false)}]])))

;; NOTE: This is a workaround and should be fixed
;; currently the issue is that a user is selected by navigating to the user route
;; on mount it is checked if user uid exists in query params
;; if so the modal opens
;; it would be nicer if the user selection would happen in the modal with a e.g. a combobox
(def show* (reagent/atom false))

(defn check-user-chosen []
  (when (contains?
         (get @routing/state* :query-params) :user-uid)
    (reset! show* true)))

(defn delete-user-button []
  [:<>
   [:> Button
    {:className "ml-3"
     :variant "danger"
     :onClick #(reset! show* true)}
    "Delete User"]])

(defn delete-user-dialog []
  [delete/dialog {:show @show*
                  :onHide #(reset! show* false)}])

(defn basic-properties []
  [:div.basic-properties.mb-3
   [:h2 "Basic User Properties"]
   [:div.row
    [:div.col-md-3.mb-2
     [:hr]
     [:h3 " Image / Avatar "]
     [user-core/img-avatar-component @user-data*]]
    [:div.col-md
     [:hr]
     [:h3 "Personal Properties"]
     [user-core/personal-properties-component @user-data*]]
    [:div.col-md
     [:hr]
     [:h3 "Account Properties"]
     [user-core/account-properties-component @user-data*]]]
   [:div.mt-3
    [:> ButtonGroup {:className "mr-3"}
     [edit-user-button]
     [reset-password-button]]
    [delete-user-button]]])

(defn inventory-pools []
  [:section
   [:h2.mt-5.mb-3 "Inventory Pools"]
   [inventory-pools/table-component]])

(defn groups []
  [:div
   [:h2.mt-5.mb-3 [:a {:href (path :groups {} {:including-user @user-id*})}
                   "Groups"]]
   [groups/table-component]])

(defn extended-info []
  [:div.row
   [:div.col-md
    [:h2.mt-5.mb-3 "Extended User Info"]
    (if-let [ext-info (some-> @user-data* :extended_info presence
                              (->> (.parse js/JSON)) presence)]
      [:div.bg-light [:pre (.stringify js/JSON ext-info nil 2)]]
      [:div.alert.alert-secondary.text-center
       "There is no extended info available for this user."])]])

(defn page []
  [:article.users
   [routing/hidden-state-component
    {:did-mount #(do
                   (clean-and-fetch)
                   (check-user-chosen))}]
   [:header.my-5
    [back/button {:href (path :users {})}]
    [:h1.mt-3 (when-not (empty? @user-data*)
                [user-core/name-component @user-data*])]]
   [basic-properties]
   [inventory-pools]
   [groups]
   [extended-info]
   [delete-user-dialog]
   [user-core/debug-component]])
