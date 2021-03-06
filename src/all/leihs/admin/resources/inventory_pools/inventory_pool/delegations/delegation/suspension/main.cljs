(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.suspension.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]
    [leihs.core.icons :as icons]

    [leihs.admin.common.components :as components]
    [leihs.admin.common.form-components :as form-components]
    [leihs.admin.common.http-client.core :as http-client]
    [leihs.admin.paths :as paths :refer [path]]
    [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
    [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.core :as delegation]
    [leihs.admin.resources.inventory-pools.inventory-pool.suspension.core :as core]
    [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.breadcrumbs :as breadcrumbs]
    [leihs.admin.resources.users.user.core :as user :refer [user-id* user-data*]]
    [leihs.admin.state :as state]
    [leihs.admin.utils.misc :refer [humanize-datetime-component wait-component]]
    [leihs.admin.utils.regex :as regex]

    ["date-fns" :as date-fns]
    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.pprint :refer [pprint]]
    [reagent.core :as reagent]
    [taoensso.timbre :as logging]))

(defonce data* (reagent/atom nil))

(defn delegation-page-suspension-component []
  [:div
   (let [suspension-path (path :inventory-pool-delegation-suspension
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
   [delegation/name-link-component]
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
  [:div.inventory-pool-delegation-suspension
   [breadcrumbs/nav-component
    (conj @breadcrumbs/left* [breadcrumbs/suspension-li])[]]
   [header-component]
   [delegation-page-suspension-component]
   [debug-component]])
