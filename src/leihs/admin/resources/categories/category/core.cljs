(ns leihs.admin.resources.categories.category.core
  (:require
   ["/admin-ui" :as UI]
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.categories.category.image :as image]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Form InputGroup Row Col Button]]
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

(defn parent-category [data* parent]
  [:> Form.Group
   [:> InputGroup
    [:> InputGroup.Prepend {:class-name "rounded mr-3"}
     (if (:thumbnail_url parent)
       [:img  {:style {:height "35px"
                       :object-fit "contain"
                       :aspect-ratio "1/1"
                       :display "flex"}
               :src (:thumbnail_url parent)}]

       [:div {:style {:font-size "0.5rem"
                      :height "35px"
                      :aspect-ratio "1/1"
                      :display "flex"
                      :align-items "center"
                      :text-align "center"}
              :class-name "border rounded"} "no picture"])]

    [:> Form.Control {:style {:border-top-left-radius "0.25rem"
                              :border-bottom-left-radius "0.25rem"}
                      :type "text"
                      :readOnly true
                      :class-name "bg-white"}]

    [:> InputGroup.Append
     [:> Button {:variant "warning"
                 :on-click #(swap! data* update
                                   :parents
                                   (fn [parents]
                                     (filter (fn [element] (not= (:id element)
                                                                 (:id parent)))
                                             parents)))} "Remove"]]]])

(defn form [action data*]
  (let [show-category-tree* (reagent/atom false)]
    (fn []
      [:> Form {:id "category-form"
                :on-submit (fn [e] (.preventDefault e) (action))}
       [:> Row
        [:> Col
         [:> Form.Group {:control-id "name"}

          [:> Form.Label {:class-name "h5"} "Name"]
          [:input.form-control
           {:id "name"
            :type "text"
            :required true
            :placeholder "Enter Name"
            :value (or (:name @data*) "")
            :onChange (fn [e] (swap! data* assoc :name (-> e .-target .-value)))}]]

         [:> Form.Group {:control-id "description"}
          [:> Form.Label {:class-name "h5"} "Image"]
          [image/component data*]]]

        [:> Col
         (when (seq (:parents @data*))
           [:div {:class-name "mb-3"}
            [:> Form.Label {:class-name "h5 mb-0"} "Parent assignments"]
            [:> Form.Text  "Selected Parent Categories"]])

         (for [parent (:parents @data*)]
           ^{:key (:id parent)}
           [parent-category data* parent])

         (if (not @show-category-tree*)
           [:> Button {:on-click #(reset! show-category-tree* true)} "Add parent category"]
           [:> UI/Components.TreeView
            {:onSelected #(do (reset! show-category-tree* false)
                              (swap! data* update :parents
                                     (fnil (fn [parents]
                                             (->> (conj parents
                                                        {:id (.. % -metadata -id)
                                                         :thumbnail_url (.. % -metadata -thumbnail_url)})

                                                  (group-by :id)
                                                  (vals)
                                                  (map first)
                                                  (vec))) [])))}])]]])))

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
