(ns leihs.admin.resources.inventory-pools.inventory-pool.opening-times.main
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.common.roles.components :refer [put-roles< roles-component]]
   [leihs.admin.common.roles.core :as roles]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.holidays.main :as holidays]
   [leihs.admin.resources.inventory-pools.inventory-pool.nav :as nav]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as workdays-core]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.main :as workdays]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.inventory-pool-debug
     [:hr]
     [:div.inventory-pool-workdays-data
      [:h3 "@workdays/data*"]
      [:pre (with-out-str (pprint @workdays-core/data*))]]]))

(defn page []
  [:article.inventory-pool-opening-times
   [routing/hidden-state-component
    {:did-mount (fn [_]
                  (js/console.log "opening times")
                  #_(inventory-pool/clean-and-fetch users/fetch-users))}]
   [:header.my-5
    [:h1.mt-3 [inventory-pool/name-component]]]
   [nav/tabs]
   [:article.my-5
    [workdays/component]
    [holidays/component]]
   [debug-component]])
