(ns leihs.admin.resources.users.choose-main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]
    [leihs.core.url.query-params :as query-params]

    [leihs.admin.common.breadcrumbs :as breadcrumbs]
    [leihs.admin.paths :as paths :refer [path]]
    [leihs.admin.resources.users.main :as users-main]
    [leihs.admin.resources.users.shared :as users-shared]

    [taoensso.timbre :as logging]
    ))

(defn choose-user-th-component []
  [:th {:key :choose} "Choose"])

(defn choose-user-td-component [user]
  [:td {:key :choose}
   [:a.btn.btn-sm.btn-primary
    {:href (let [uid (or (:email user) (:login user)  (:id user))]
             (str (-> @routing/state* :query-params-raw :referer)
                  "?" (-> @routing/state*
                          :query-params-raw
                          (dissoc :referer)
                          (assoc :chosen-user-uid uid)
                          query-params/encode )))}
    [:i.fas.fa-rotate-90.fa-hand-pointer]
    " Choose user "]])

(defn user-td-component [user]
  [:td [users-main/user-td-inner-component user]])

(defn table []
  [users-main/table-component
   [users-main/user-th-component
    choose-user-th-component]
   [user-td-component
    choose-user-td-component]])

(defn page []
  [:div
   [breadcrumbs/nav-component
    [[breadcrumbs/leihs-li]
     [breadcrumbs/admin-li]
     [breadcrumbs/users-li]
     [breadcrumbs/users-choose-li]][]]
   [routing/hidden-state-component
    {:did-mount users-main/escalate-query-paramas-update
     :did-update users-main/escalate-query-paramas-update}]

   [:div.card.bg-light
    [:div.card-body
     [:div.form-row
      [users-main/form-term-filter]
      [users-main/form-org-filter]
      [users-main/form-enabled-filter]
      [users-main/form-admins-filter]
      [routing/form-per-page-component]
      [routing/form-reset-component
       :default-query-params users-shared/default-query-params
       ]]]]

   [routing/pagination-component]

   [table]
   [routing/pagination-component]])