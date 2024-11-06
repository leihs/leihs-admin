(ns leihs.admin.resources.categories.main
  (:require
   ;; ["react-accessible-treeview" :as tree-view :refer [TreeView]]

   ["/admin-ui" :as UI]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.inventory-pools.inventory-pool.create :as create]
   [leihs.admin.resources.inventory-pools.shared :as shared]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component fetch-route*]]
   [leihs.core.auth.core :as auth-core]
   [leihs.core.json :as json]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

(def current-query-parameters*
  (reaction (-> @routing/state* :query-params
                (assoc :term (-> @routing/state* :query-params-raw :term)
                       :order (some-> @routing/state* :query-params
                                      :order clj->js json/to-json)))))

(def current-query-parameters-normalized*
  (reaction (shared/normalized-query-parameters @current-query-parameters*)))

(def current-route*
  (reaction
   (path (:handler-key @routing/state*)
         (:route-params @routing/state*)
         @current-query-parameters-normalized*)))

(def data* (reagent/atom nil))

(defn fetch []
  (http-client/route-cached-fetch
   data* {:route @fetch-route*
          :reload true}))

;;; helpers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn page-path-for-query-params [query-params]
  (path (:handler-key @routing/state*)
        (:route-params @routing/state*)
        (merge @current-query-parameters-normalized*
               query-params)))

;;; Filter ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn debug-component []
  (when (:debug @state/global-state*)
    [:section.debug
     [:hr]
     [:h2 "Page Debug"]
     [:div
      [:h3 "@current-query-parameters-normalized*"]
      [:pre (with-out-str (pprint @current-query-parameters-normalized*))]]
     [:div
      [:h3 "@current-route*"]
      [:pre (with-out-str (pprint @current-route*))]]
     [:div
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-change #(fetch)}]

   [:article.categories

    [:header.my-5
     [:h1
      [icons/categories] " Categories"]]

    [:section
     [:> UI/Components.TreeView]]

    [:section
     [create/dialog]
     [debug-component]]]])
