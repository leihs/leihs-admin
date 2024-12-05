(ns leihs.admin.resources.categories.category.core
  (:require
   ["/admin-ui" :as UI]
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [clojure.string :as str]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.categories.category.image :as image]
   [leihs.admin.resources.categories.filter :as tree-filter]
   [leihs.admin.resources.categories.tree :as tree-path]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Form InputGroup Row Col Button Container Card]]
   [reagent.core :as reagent :refer [reaction]]))

;;; atoms ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce id*
  (reaction (or (-> @routing/state* :route-params :category-id presence)
                ":category-id")))

(defonce cache* (reagent/atom nil))

(defonce path*
  (reaction (path :category {:category-id @id*})))

(defonce data*
  (reaction (get @cache* @path*)))

(defonce data-models* (reagent/atom nil))

(defonce categories-cache* (reagent/atom nil))

(defonce categories-data*
  (reaction (get @categories-cache* "/admin/categories/")))

;;; fetch data ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch []
  (http-client/route-cached-fetch cache* {:route @path*}))

(defn fetch-models []
  (go (reset! data-models*
              (some-> {:chan (async/chan)
                       :url (path :category-models {:category-id @id*})}
                      http-client/request
                      :chan <!
                      http-client/filter-success!
                      :body :models))))

;;; form ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn parent-category [form-data* parent]
  (let [tree @categories-data*
        parent-tree (first (tree-filter/deep-filter #(= (:category_id %)
                                                        (:id parent))
                                                    (:children tree)))
        path (str/join " <- " (map :name (if (vector? parent)
                                           parent
                                           (tree-path/convert-tree-path parent-tree))))]

    [:> Card {:class-name "mb-3"}
     [:> Container {:class-name "p-2"}
      [:button {:type "button"
                :class-name "close"
                :style {:z-index "9999"
                        :position "absolute"
                        :right "0.5rem"}
                :on-click #(swap! form-data* update
                                  :parents
                                  (fn [parents]
                                    (filter (fn [element] (not= (:id element)
                                                                (:id parent)))
                                            parents)))} [icons/delete]]
      [:> Row
       [:> Col (if (:thumbnail_url parent)
                 [:img  {:style {:object-fit "contain"
                                 :aspect-ratio "1/1"
                                 :display "flex"}
                         :src (:thumbnail_url parent)}]

                 [:div {:style {:font-size "0.5rem"
                                :aspect-ratio "1/1"
                                :display "flex"
                                :align-items "center"
                                :justify-content "center"}
                        :class-name "border rounded"} [:span "no picture"]])]
       [:> Col {:sm 10}
        [:p {:class-name "mr-3"} (if (not (str/blank? path)) path (:name parent))]
        [:> Form.Group

         [:input.form-control
          {:id "label"
           :type "text"
           :placeholder "Enter Label"
           :value (or (:label parent) "")
           :onChange #(swap! form-data* update
                             :parents
                             (fn [parents]
                               (map (fn [el]
                                      (if (= (:id parent) (:id el))
                                        (assoc el :label (.. % -target -value))                                                                    el))
                                    parents)))}]]]]]]))

(defonce show-category-tree* (reagent/atom false))

(defn form [action form-data*]
  [:> Form {:id "category-form"
            :on-submit (fn [e] (.preventDefault e) (action))}
   [:> Row
    [:> Col {:sm 4}
     [:> Form.Group {:control-id "name"}

      [:> Form.Label {:class-name "h5"} "Name"]
      [:input.form-control
       {:id "name"
        :type "text"
        :required true
        :placeholder "Enter Name"
        :value (or (:name @form-data*) "")
        :onChange (fn [e] (swap! form-data* assoc :name (-> e .-target .-value)))}]]

     [:> Form.Group {:control-id "description"}
      [:> Form.Label {:class-name "h5"} "Image"]
      [image/component form-data*]]]

    [:> Col
     (when (seq (:parents @form-data*))
       [:div {:class-name "mb-3"}
        [:> Form.Label {:class-name "h5 mb-0"} "Parent assignments"]
        [:> Form.Text  "Selected Parent Categories"]])

     (for [parent (:parents @form-data*)]
       ^{:key (:id parent)}
       [parent-category form-data* parent])

     (if (not @show-category-tree*)
       [:> Button {:on-click #(reset! show-category-tree* true)} "Add parent category"]
       [:> UI/Components.TreeView
        {:data (clj->js @categories-data*)
         :onSelected #(do (reset! show-category-tree* false)
                          (swap! form-data* update :parents
                                 (fnil (fn [parents]
                                         (->> (conj parents
                                                    {:id (.. % -metadata -id)
                                                     :name (.. % -metadata -name)
                                                     :thumbnail_url (.. % -metadata -thumbnail_url)})

                                              (group-by :id)
                                              (vals)
                                              (map first)
                                              (vec))) [])))}])]]])

;;; debug ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.container.text-break
     [:div.category-debug
      [:hr]
      [:div.category-data
       [:h2 "@data*"]
       [:pre (with-out-str (pprint @data*))]]]
     [:div.category-debug
      [:hr]
      [:div.category-data
       [:h2 "@category-models-data*"]
       [:pre (with-out-str (pprint @data-models*))]]]]))
