(ns leihs.admin.resources.settings.languages.edit
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.resources.settings.languages.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(def data* (reagent/atom nil))

(defn put []
  (let [ch (async/chan)]
    (requests/send-off {:json-params @data* :method :put} {} :chan ch)
    (go (when (:success (<! ch))
          (reset! core/data* @data*)
          (search-params/delete-from-url "action")))))

(def open?*
  (reaction
   (reset! data* @core/data*)
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn dialog [& {:keys [show onHide]
                 :or {show false}}]
  ;; (core/fetch)
  [:> Modal {:size "lg"
             :centered true
             :scrollable true
             :show @open?*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url
                                "action")}
    [:> Modal.Title "Edit Languages"]]
   [:> Modal.Body
    [core/form put data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url
                            "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "languages-form"}
     "Save"]]])

(defn button []
  [:> Button
   {:on-click #(search-params/append-to-url
                {:action "edit"})}
   "Edit"])
