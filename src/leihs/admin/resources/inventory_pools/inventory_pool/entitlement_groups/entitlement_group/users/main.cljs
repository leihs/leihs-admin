(ns leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.users.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.navigation.back :refer [back]]
   [leihs.admin.common.components.pagination :refer [pagination]]
   [leihs.admin.common.membership.users.main :as membership-users]
   [leihs.admin.common.membership.users.shared :refer [DEFAULT-MEMBERSHIP-QUERY-PARAM
                                                       MEMBERSHIP-QUERY-PARAM-KEY QUERY-OPTIONS]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.core :as entitlement-group]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.main :as pool-users]
   [leihs.admin.resources.users.main :as users]
   [leihs.core.core :refer [presence]]
   [react-bootstrap :as BS]))

;;; direct member ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn direct-member-path-fn [user]
  (path :inventory-pool-entitlement-group-direct-user
        {:inventory-pool-id @inventory-pool/id*
         :entitlement-group-id @entitlement-group/id*
         :user-id (:id user)}))

;;; group member ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn groups-path-fn
  ([user] (groups-path-fn user {} {}))
  ([user more-route-params more-query-params]
   (path :inventory-pool-entitlement-group-groups
         (merge {:inventory-pool-id @inventory-pool/id*
                 :entitlement-group-id @entitlement-group/id*}
                more-route-params)
         (merge {:including-user (or (-> user :email presence) (:id user))}
                more-query-params))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn table-toolbar []
  [:> react-bootstrap/ButtonToolbar {:className "my-3"}
   [pagination]])

(defn users-component []
  [:div.entitlement-group-users
   [users/filter-component]
   [table-toolbar]
   [users/users-table
    [pool-users/user-th-component
     membership-users/member-user-th-component
     membership-users/direct-member-user-th-component
     membership-users/group-member-user-th-component]
    [pool-users/user-td-component
     membership-users/member-user-td-component
     (membership-users/create-direct-member-user-td-component
      direct-member-path-fn)
     (membership-users/create-group-member-user-td-component
      groups-path-fn)]
    :membership-filter? true]
   [table-toolbar]])

(defn header []
  [:header.mt-5
   [back]
   [:h1.mb-5.mt-3
    [entitlement-group/name-component]
    [:span " Users in "]
    [inventory-pool/name-component]]])

(defn page []
  [:article.inventory-pool-entitlement-group-users
   [header]
   [users-component]
   [users/debug-component]])
