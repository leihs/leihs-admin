(ns leihs.admin.resources.users.breadcrumbs
  (:require [leihs.admin.common.icons :as icons]
            [leihs.admin.resources.breadcrumbs :as breadcrumbs]
            [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
            [leihs.core.auth.core :as auth]
            [reagent.core :as reagent :refer [reaction]]))

(def li breadcrumbs/li)
(def nav-component breadcrumbs/nav-component)

(defn users-choose-li []
  [li :users-choose [:span  " Choose user "] {} {}
   :authorizers [auth/admin-scopes?]])

(defn users-li []
  [li :users [:span [icons/users] " Users "] {} {}
   :authorizers [auth/admin-scopes? pool-auth/some-lending-manager?]])

(defonce left*
  (reaction
   (conj @breadcrumbs/left* [users-li])))
