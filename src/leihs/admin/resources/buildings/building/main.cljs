(ns leihs.admin.resources.buildings.building.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.admin.common.components.navigation.back :as back]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.buildings.building.core :as building :refer [clean-and-fetch data*]]
   [leihs.admin.resources.buildings.building.delete :as delete]
   [leihs.admin.resources.buildings.building.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button]]
   [reagent.core :as reagent]))

(defn building-info-table []
  (if-not @building/data*
    [wait-component]
    [table/container
     {:borders false
      :header [:tr [:th "Property"] [:th.w-75 "Value"]]
      :body
      [:<>
       [:tr.name
        [:td "Name" [:small " (name)"]]
        [:td.name (clojure.core/str  (:name @building/data*))]]
       [:tr.code
        [:td "Code" [:small " (code)"]]
        [:td.code
         (:code @building/data*)]]]}]))

(defn edit-building-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:onClick #(reset! show true)}
        "Edit Building"]
       [edit/dialog {:show @show
                     :onHide #(reset! show false)}]])))

(defn delete-building-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:variant "danger"
         :className "ml-3"
         :onClick #(reset! show true)}
        "Delete Building"]
       [delete/dialog {:show @show
                       :onHide #(reset! show false)}]])))

;;; show ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn page []
  [routing/hidden-state-component {:did-mount #(clean-and-fetch)}]
  [:article.building
   [:header.my-5
    [back/button {:href (path :buildings)}]
    [:h1.mt-3
     [building/building-name]]]
   [building-info-table]
   [edit-building-button]
   [delete-building-button]
   [building/debug-component]])
