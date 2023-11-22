(ns leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.groups.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.navigation.back :refer [back]]
   [leihs.admin.common.components.pagination :refer [pagination]]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.common.membership.groups.main :as groups-membership]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.main :as groups]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.core :as entitlement-group]
   [leihs.admin.state :as state]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap]))

(defn member-path [group]
  (path :inventory-pool-entitlement-group-group
        {:inventory-pool-id @inventory-pool/id*
         :entitlement-group-id @entitlement-group/id*
         :group-id (:id group)}))

;### filter ###################################################################

(defn filter-component []
  [filter/container
   [:<>
    [groups/form-term-filter]
    [groups/form-including-user-filter]
    [groups-membership/form-membership-filter]
    [filter/form-per-page]
    [filter/reset]]])

;### header ###################################################################

(defn header-component []
  [:div.mt-5
   [back]
   [:h1.mb-5.mt-3
    [:span "Groups of "]
    [entitlement-group/name-component]
    [:span " in "]
    [inventory-pool/name-component]]])

;### table toolbar ############################################################

;; TODO: not the correct add group function
(defn add-group-button []
  [:> react-bootstrap/Button
   {:href (path
           :inventory-pool-user-create
           {:inventory-pool-id @inventory-pool/id*})
    :variant "primary"
    :className "ml-4"}
   [:span [icons/add] " Add Group"]])

(defn table-toolbar []
  [:> react-bootstrap/ButtonToolbar {:className "my-3"}
   [pagination]
   [add-group-button]])

;### main #####################################################################

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div]))

(defn main-page-component []
  [:div
   [routing/hidden-state-component
    {:did-change groups/fetch-groups}]
   [filter-component]
   [table-toolbar]
   [groups/table-component
    [groups/name-th-component
     groups-membership/member-th-component]
    [groups/name-td-component
     (partial groups-membership/member-td-component member-path)]]
   [table-toolbar]
   [debug-component]
   [groups/debug-component]])

(defn page []
  [:div.inventory-pool-groups
   [routing/hidden-state-component
    {:did-mount (fn [_] (inventory-pool/clean-and-fetch))}]
   [:<>
    [header-component]
    [main-page-component]]])
