(ns leihs.admin.resources.categories.category.main
  (:require
   [clojure.string :as str]
   [leihs.admin.common.components.navigation.breadcrumbs :as breadcrumbs]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.categories.category.core :as core]
   [leihs.admin.resources.categories.category.delete :as delete]
   [leihs.admin.resources.categories.category.edit :as edit]
   [leihs.admin.resources.categories.category.models.main :as models-main]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :refer [OverlayTrigger Tooltip]]))

(defn path-from-parents [parents]
  (for [parent parents]
    (let [parent-paths (map #(:name %) parent)
          labels (map #(or (-> % :metadata :label) "") parent)
          parents-labes (map (fn [el]
                               {:name (:name el)
                                :label (-> el :metadata :label)})
                             parent)
          combined (map (fn [p l] (if
                                   (empty? l)
                                    p
                                    l))
                        parent-paths labels)]

      (js/console.debug (str/join parents-labes))
      (str/join " <- " combined))))

(defn parents-labels [parents]
  (for [parent parents]
    (map-indexed (fn [idx el]
                   {:index idx
                    :name (:name el)
                    :label (-> el :metadata :label)})
                 parent)))

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
      [:tr.parents
       [:td [:strong "Parents"] [:small " (metadata:parents)"]]
       [:td [:ul (for [path (parents-labels (-> @core/data* :parents))]
                   ^{:key (str path)}
                   [:li
                    (for [parent path]
                      ^{:key (:index parent)}
                      [:span
                       (when (> (:index parent) 0) " <- ")

                       (if (:label parent)

                         [:span (:name parent) " "
                          [:span {:class-name "badge badge-info"} (:label parent)]]
                         (:name parent))])])]]]

      [:tr.image
       [:td [:strong "Image"] [:small " (metadata:image_url)"]]
       [:td [:img {:src (-> @core/data* :metadata :image_url)}]]]]}]])

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
       (when (-> @core/data* :metadata :is_deletable)
         [delete/button])
       [delete/dialog]
       [:div.mt-5.mb-5
        [models-main/models-in-category-table]]
       [core/debug-component]]])])

