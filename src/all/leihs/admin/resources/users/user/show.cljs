(ns leihs.admin.resources.users.user.show
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.routing.front :as routing]

    [leihs.admin.common.breadcrumbs :as breadcrumbs-common]
    [leihs.admin.paths :as paths :refer [path]]
    [leihs.admin.resources.audits.changes.breadcrumbs :as audited-changes-breadcrumbs]
    [leihs.admin.resources.users.user.breadcrumbs :as breadcrumbs]
    [leihs.admin.resources.users.user.core :as user-core :refer [clean-and-fetch user-id* user-data*] ]
    [leihs.admin.resources.users.user.groups :as groups]
    [leihs.admin.resources.users.user.inventory-pools :as inventory-pools]
    [leihs.admin.state :as state]

    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.core.async :refer [timeout]]
    [cljs.pprint :refer [pprint]]
    [clojure.contrib.inflect :refer [pluralize-noun]]
    [reagent.core :as reagent]
    ))

(defn page []
  [:div.user
   [routing/hidden-state-component
    {:did-mount clean-and-fetch}]
   [breadcrumbs/nav-component
     @breadcrumbs/left*
     [[breadcrumbs-common/email-li (:email @user-data*)]
      [breadcrumbs/user-my-li]
      [audited-changes-breadcrumbs/changes-li
       :query-params {:pkey (:id @user-data*)
                      :table "users"}]
      [breadcrumbs/delete-li]
      [breadcrumbs/edit-li]]]
   [:h1 " User " (when @user-data* [user-core/name-component @user-data*])]
   [:div.basic-properties.mb-2
    [:h3 "Basic User Properties"]
    [:div.row
     [:div.col-md-3.mb-1
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
      [user-core/account-properties-component @user-data*]]
     ]]
   [:div
    [:hr]
    [:h2 "Inventory Pools"]
    [inventory-pools/table-component]]
   [:div
    [:hr]
    [:h2 "Groups"]
    [groups/table-component]
    ]
   [:div.row
    [:div.col-md
     [:h2 "Extended User Info"]
     (when-let [ext-info (-> @user-data* :extended_info presence)]
       [:pre (.stringify js/JSON (.parse js/JSON ext-info) nil 2)])]]
   [user-core/debug-component]])

