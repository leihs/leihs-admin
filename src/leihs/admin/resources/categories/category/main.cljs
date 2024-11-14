(ns leihs.admin.resources.categories.category.main
  (:require
   [leihs.admin.common.components.navigation.breadcrumbs :as breadcrumbs]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.categories.category.core :as core]
   [leihs.admin.resources.categories.category.delete :as delete]
   [leihs.admin.resources.categories.category.edit :as edit]
   [leihs.admin.resources.categories.category.models.main :as models-main]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn info-table []
  [:<>
   [table/container
    {:borders false
     :header [:tr [:th "Property"] [:th.w-75 "Value"]]
     :body
     [:<>
      [:tr.name
       [:td [:strong "Name"] [:small " (name)"]]
       [:td (str (:name @core/data*))]]
      [:tr.label
       [:td [:strong "Label"] [:small " (metadata:label)"]]
       [:td (-> @core/data* :metadata :label)]]
      [:tr.thumgnail_url
       [:td [:strong "Thumbnail URL"] [:small " (metadata:thumbnail_url)"]]
       [:td [:img {:src (-> @core/data* :metadata :thumbnail_url)}]]]]}]])

(defn header []
  [:header.my-5
   [breadcrumbs/main  {:to (path :categories)}]
   [:h1.mt-3 (-> @core/data* :name)]
   (let [label (-> @core/data* :metadata :label)]
     (when label [:p "( " label " )"]))])

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount (fn []
                  (core/fetch-models)
                  (core/fetch))}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]

     [:article.room
      [header]
      [:section
       [info-table]
       [edit/button]
       [edit/dialog]
       [delete/button]
       [delete/dialog]
       [:div.mt-5.mb-5
        [models-main/models-in-category-table]]
       [core/debug-component]]])])

