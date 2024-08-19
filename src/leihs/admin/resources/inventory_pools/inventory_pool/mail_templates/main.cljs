(ns leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.main
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.http-client.core :as http]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.mail-templates.main :as global]
   [leihs.admin.resources.mail-templates.shared :as shared]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [fetch-route* wait-component]]
   [leihs.core.auth.core :as auth]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

(def current-query-parameters*
  (reaction (-> @routing/state* :query-params
                (assoc :term (-> @routing/state* :query-params-raw :term)))))

(defonce pool-id*
  (reaction (or (-> @routing/state* :route-params :inventory-pool-id presence)
                ":inventory-pool-id")))

(def current-url* (reaction (:route @routing/state*)))

(def current-query-parameters-normalized*
  (reaction (shared/normalized-query-parameters @current-query-parameters*)))

(def data* (reagent/atom nil))
(defonce languages-data* (reagent/atom nil))

(defn fetch []
  (http/route-cached-fetch data* {:route @fetch-route*
                                  :reload true}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn name-td-component [mail-template]
  [:td {:key :name}
   [global/link-to-mail-template
    mail-template
    #(path :inventory-pool-mail-template {:mail-template-id (:id %)
                                          :inventory-pool-id @pool-id*})
    [:span (:name mail-template)]
    :authorizers [auth/admin-scopes? pool-auth/pool-inventory-manager?]]])

(defn table-component [hds tds]
  (if-not (contains? @data* @fetch-route*)
    [wait-component]
    [global/core-table-component
     (-> @data* (get @fetch-route*) :mail-templates)
     name-td-component]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn debug-component []
  (when (:debug @state/global-state*)
    [:section.debug
     [:hr]
     [:h2 "Page Debug"]
     [:div
      [:h3 "@current-query-parameters-normalized*"]
      [:pre (with-out-str (pprint @current-query-parameters-normalized*))]]
     [:div
      [:h3 "@current-url*"]
      [:pre (with-out-str (pprint @current-url*))]]
     [:div
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]
     [:div
      [:h3 "@languages-data*"]
      [:pre (with-out-str (pprint @languages-data*))]]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-change #(fetch)
     :did-mount #(do (pool-core/fetch) (global/fetch-languages))}]

   [:article.mail-templates
    [pool-core/header]

    [:section.mb-5
     [pool-core/tabs]
     [global/filter-component]
     [table-component]
     [debug-component]]]])
