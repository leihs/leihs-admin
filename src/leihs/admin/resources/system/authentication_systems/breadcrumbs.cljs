(ns leihs.admin.resources.system.authentication-systems.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.system.breadcrumbs :as breadcrumbs]
   [leihs.core.auth.core :as auth]
   [leihs.core.core :refer [keyword str presence]]

   [leihs.core.routing.front :as routing]
   [leihs.core.user.front :as core-user]))

(def li breadcrumbs/li)
(def nav-component breadcrumbs/nav-component)

(defn create-li []
  [li :authentication-system-create
   [:span [icons/add] " Create Authentication-System "] {} {}
   :authorizers [auth/system-admin-scopes?]])

(defn authentication-systems-li []
  [li :authentication-systems
   [:span [icons/authentication-systems] " Authentication-Systems "] {} {}
   :authorizers [auth/system-admin-scopes?]])

(defonce left*
  (reaction
   (conj @breadcrumbs/left* [authentication-systems-li])))
