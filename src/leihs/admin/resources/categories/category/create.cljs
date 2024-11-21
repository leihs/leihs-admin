(ns leihs.admin.resources.categories.category.create
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.categories.category.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(def data* (reagent/atom nil))

(defn create []
  (js/console.debug @data*)
  #_(go (when-let [id (some->
                       {:url (path :categores)
                        :method :post
                        :json-params  @data*
                        :chan (async/chan)}
                       http-client/request :chan <!
                       http-client/filter-success!
                       :body :id)]
          (accountant/navigate!
           (path :category {:category-id id})))))

(def open?*
  (reaction
   (->> (:query-params @routing/state*)
        :action
        (= "add"))))

(defn dialog []
  [:> Modal {:size "xl"
             :centered true
             :scrollable true
             :show @open?*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url
                                "action")}
    [:> Modal.Title "Add a Category"]]
   [:> Modal.Body
    (js/console.debug @data*)
    [core/form create data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url
                            "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "category-form"}
     "Save"]]])

(defn button []
  [:<>
   [:> Button
    {:className "mb-3"
     :on-click #(search-params/append-to-url
                 {:action "add"})}
    "Add Category"]])
