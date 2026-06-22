(ns leihs.admin.resources.groups.group.create
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.group.core :as core]
   [leihs.admin.resources.groups.group.edit-core :as edit-core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Form Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(defn post []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :groups)
                        :method :post
                        :json-params @data*} {} :chan ch)
    (go (let [resp (<! ch)]
          (when (:success resp)
            (search-params/delete-from-url "action")
            (accountant/navigate!
             (path :group {:group-id (-> resp :body :id)})))))))

(def open*
  (reaction
   (reset! data* @core/data*)
   (->> (:query-params @routing/state*)
        :action
        (= "add"))))

(defn dialog []
  [:> Modal {:size "xl"
             :centered true
             :scrollable true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url
                                "action")}
    [:> Modal.Title "Add a new Group"]]

   [:> Modal.Body
    [:> Form {:id "add-group-form"
              :on-submit (fn [e] (.preventDefault e) (post))}
     [edit-core/inner-form-component data*]]]

   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url
                            "action")}
     "Cancel"]

    [:> Button {:type "submit"
                :form "add-group-form"}
     "Add"]]])

(defn button []
  [:<>
   [:> Button
    {:className "ml-3"
     :on-click #(search-params/append-to-url
                 {:action "add"})}
    "Add Group"]])
