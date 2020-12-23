(ns leihs.admin.resources.groups.group.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.routing.front :as routing]
    [leihs.core.icons :as icons]
    [leihs.core.auth.core :as auth]

    [leihs.admin.paths :as paths :refer [path]]
    [leihs.admin.resources.groups.breadcrumbs :as breadcrumbs]
    [leihs.admin.resources.groups.group.core :refer [data*]]
    [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
    [leihs.admin.state :as state]

    [cljs.pprint :refer [pprint]]
    [cljs.core.async :as async :refer [timeout]]
    [clojure.string :refer [split trim]]
    [reagent.core :as reagent]
    [taoensso.timbre :as logging]
    ))

(def li breadcrumbs/li)
(def nav-component breadcrumbs/nav-component)

(defonce group-id*
  (reaction (or (-> @routing/state* :route-params :group-id)
                "group-id")))

(defn some-lending-manager-group-unprotected? [current-group-state _]
  (and (pool-auth/some-lending-manager? current-group-state _)
       (boolean  @data*)
       (-> @data* :protected not)))



(defn edit-li []
  [breadcrumbs/li :group-edit [:span icons/edit " Edit "]
   {:group-id @group-id*} {}
   :button true
   :authorizers [auth/admin-scopes?
                 some-lending-manager-group-unprotected?]])

(defn delete-li []
  [breadcrumbs/li :group-delete [:span icons/delete " Delete "]
   {:group-id @group-id*} {}
   :button true
   :authorizers [auth/admin-scopes?
                 some-lending-manager-group-unprotected?]])

(defn group-li []
  [li :group [:span icons/group " Group "]
   {:group-id @group-id*} {}
   :authorizers [auth/admin-scopes?
                 pool-auth/some-lending-manager?]])

(defn users-li []
  [li :group-users [:span icons/users " Users "]
   {:group-id @group-id*} {}
   :authorizers [auth/admin-scopes?
                 pool-auth/some-lending-manager?]])

(defonce left*
  (reaction
    (conj @breadcrumbs/left* [group-li])))
