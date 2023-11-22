(ns leihs.admin.resources.buildings.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.buildings.building.create :as create]
   [leihs.admin.resources.buildings.shared :as shared]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.auth.core :as auth]
   [leihs.core.core :refer [str]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as BS :refer [Button]]
   [reagent.core :as reagent]))

(def current-query-paramerters*
  (reaction (-> @routing/state* :query-params
                (assoc :term (-> @routing/state* :query-params-raw :term)))))

(def current-url* (reaction (:route @routing/state*)))

(def current-query-paramerters-normalized*
  (reaction (shared/normalized-query-parameters @current-query-paramerters*)))

(def data* (reagent/atom {}))

(defn fetch-buildings []
  (http/route-cached-fetch data*))

;;; helpers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn page-path-for-query-params [query-params]
  (path (:handler-key @routing/state*)
        (:route-params @routing/state*)
        (merge @current-query-paramerters-normalized*
               query-params)))

(defn link-to-building
  [building inner & {:keys [authorizers]
                     :or {authorizers []}}]
  (if (auth/allowed? authorizers)
    [:a {:href (path :building {:building-id (:id building)})} inner]
    inner))

;;; Filter ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn filter-component []
  [filter/container
   [:<>
    [filter/form-term-filter-component {:placeholder "Search for buildings"}]
    [filter/form-per-page]
    [filter/reset]]])

;;; Table ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn name-th-component []
  [:th {:key :name} "Name"])

(defn name-td-component [building]
  (let [building-name (cond-> (:name building)
                        (:is_general building) (str " (general)"))
        inner-comp [:td {:key :code}
                    [link-to-building building [:span building-name]
                     :authorizers [auth/admin-scopes?]]]]
    (cond->> inner-comp
      (:is_general building) (vector :<>))))

(defn code-th-component []
  [:th {:key :code} "Code"])

(defn code-td-component [building]
  [:td.text-left {:key :code} (:code building)])

(defn items-count-th-component []
  [:th.text-left {:key :items_count} "# Items"])

(defn items-count-td-component [building]
  [:td.text-left {:key :items_count} (:items_count building)])

(defn rooms-count-th-component []
  [:th.text-left {:key :rooms_count} "# Rooms"])

(defn rooms-count-td-component [building]
  [:td.text-left {:key :rooms_count} (:rooms_count building)])

;;;;;

(defn add-building-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:className "ml-3"
         :onClick #(reset! show true)}
        "Add Building"]
       [create/dialog {:show @show
                       :onHide #(reset! show false)}]])))

(defn buildings-thead-component [more-cols]
  [:tr
   [:th {:key :index} "Index"]
   (for [[idx col] (map-indexed vector more-cols)]
     ^{:key idx} [col])])

(defn building-row-component [building more-cols]
  ^{:key (:id building)}
  [:tr.building {:key (:id building)}
   [:td {:key :index} (:index building)]
   (for [[idx col] (map-indexed vector more-cols)]
     ^{:key idx} [col building])])

(defn core-table-component [hds tds buildings]
  (if-let [buildings (seq buildings)]
    [table/container {:className "buildings"
                      :actions [table/toolbar [add-building-button]]
                      :header [buildings-thead-component hds]
                      :body
                      (doall (for [building buildings]
                               ^{:key (:id building)}
                               [building-row-component building tds]))}]
    [:div.alert.alert-warning.text-center "No (more) buildings found."]))

(defn table-component [hds tds]
  (if-not (contains? @data* (:route @routing/state*))
    [wait-component]
    [core-table-component hds tds
     (-> @data* (get (:route @routing/state*) {}) :buildings)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn debug-component []
  (when (:debug @state/global-state*)
    [:section.debug
     [:hr]
     [:h2 "Page Debug"]
     [:div
      [:h3 "@current-query-paramerters-normalized*"]
      [:pre (with-out-str (pprint @current-query-paramerters-normalized*))]]
     [:div
      [:h3 "@current-url*"]
      [:pre (with-out-str (pprint @current-url*))]]
     [:div
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))

(defn page []
  [:article.buildings
   [:header.my-5
    [:h1 [icons/building] " Buildings"]]
   [:section
    [routing/hidden-state-component
     {:did-change fetch-buildings}]
    [filter-component]
    [table-component
     [name-th-component
      code-th-component
      rooms-count-th-component
      items-count-th-component]
     [name-td-component
      code-td-component
      rooms-count-td-component
      items-count-td-component]]
    [debug-component]]])
