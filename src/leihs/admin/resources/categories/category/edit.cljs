(ns leihs.admin.resources.categories.category.edit
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.categories.category.core :as core]
   [leihs.admin.resources.categories.category.image :as image]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(def data* (reagent/atom nil))

(defn remove-duplicates [data]
  (->> data
       (group-by :id)
       (vals)
       (map first)
       vec))

(defn map-data [data]
  {:id (:category_id data)
   :name (:name data)
   :image (-> data :metadata :image_url)
   :parents (->> (:parents data)
                 (map (fn [el]
                        (let [parent (last (butlast el))
                              category (last el)
                              label (-> category :metadata :label)]
                          (-> parent
                              (assoc :id (:category_id parent))
                              (assoc :label label)
                              (dissoc :category_id)
                              (dissoc :metadata)))))
                 (remove-duplicates))})

(defn patch []
  (js/console.debug (conj @data* @image/data*))
  (let [route (path :category {:category-id @core/id*})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params  (conj @data* @image/data*)
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
          (swap! core/cache* assoc @core/path* @data*)
          (search-params/delete-from-url "action")))))

(def open?*
  (reaction
   (reset! data* (map-data @core/data*))
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn dialog []
  [:> Modal {:size "xl"
             :centered true
             :scrollable true
             :show @open?*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url
                                "action")}
    [:> Modal.Title "Edit Category"]]
   [:> Modal.Body
    [core/form patch data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url
                            "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "category-form"}
     "Save"]]])

(defn button []
  [:> Button
   {:on-click #(search-params/append-to-url
                {:action "edit"})}
   "Edit"])
