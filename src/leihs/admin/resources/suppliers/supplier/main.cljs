(ns leihs.admin.resources.suppliers.supplier.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.admin.common.components.navigation.back :as back]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.suppliers.supplier.core :as supplier-core :refer [clean-and-fetch data*]]
   [leihs.admin.resources.suppliers.supplier.delete :as delete]
   [leihs.admin.resources.suppliers.supplier.edit :as edit]
   [leihs.admin.resources.suppliers.supplier.items :as items]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button]]
   [reagent.core :as reagent]))

(defn info-table []
  (let [data @supplier-core/data*]
    (fn []
      [table/container
       {:borders false
        :header [:tr [:th "Property"] [:th.w-75 "Value"]]
        :body
        [:<>
         [:tr.name
          [:td "Name" [:small " (name)"]]
          [:td.name (:name data)]]
         [:tr.description
          [:td "Note" [:small " (note)"]]
          [:td.note (:note data)]]]}])))

(defn edit-building-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:onClick #(reset! show true)}
        "Edit"]
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
        "Delete"]
       [delete/dialog {:show @show
                       :onHide #(reset! show false)}]])))

(defn header []
  (let [name (:name @supplier-core/data*)]
    (fn []
      [:header.my-5
       [back/button  {:to (path :suppliers)}]
       [:h1.mt-3 name]])))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-change clean-and-fetch}]
   (if-not @supplier-core/data*
     [:div.my-5
      [wait-component " Loading Room Data ..."]]
     [:article.supplier
      [header]
      [info-table]
      [edit-building-button]
      [delete-building-button]
      [items/component]
      [supplier-core/debug-component]])])
