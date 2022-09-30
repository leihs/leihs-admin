(ns leihs.admin.resources.mail-templates.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.auth.core :as auth]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.admin.common.icons :as icons]
    [leihs.core.routing.front :as routing]

    [leihs.admin.resources.breadcrumbs :as breadcrumbs]
    [leihs.admin.paths :as paths :refer [path]]

    [cljs.pprint :refer [pprint]]
    [cljs.core.async :as async :refer [timeout]]
    [clojure.string :refer [split trim]]
    [reagent.core :as reagent]
    [taoensso.timbre :as logging]
    ))

(def li breadcrumbs/li)
(def nav-component breadcrumbs/nav-component)

(defn mail-templates-li []
  [li :mail-templates [:span [icons/mail-templates] " Mail-Templates "] {} {}
   :authorizers [auth/admin-scopes?]])

(defn create-li []
  [li :mail-template-create
   [:span [:i.fas.fa-plus-circle] " Create Mail-Template "] {} {}
   :button true
   :authorizers [auth/admin-scopes?]])

;;; main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce left*
  (reaction
    (conj @breadcrumbs/left* [mail-templates-li])))
