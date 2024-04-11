(ns leihs.admin.resources.mail-templates.mail-template.core
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Form]]
   [reagent.core :as reagent]
   [reagent.ratom :as ratom :refer [reaction]]))

(defonce id*
  (reaction (or (-> @routing/state* :route-params :mail-template-id presence)
                ":mail-template-id")))

(defonce data* (reagent/atom nil))

;;; fetch ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch []
  (go (reset! data*
              (some->
               {:chan (async/chan)
                :url (path :mail-template
                           (-> @routing/state* :route-params))}
               http-client/request :chan <!
               http-client/filter-success! :body))))

(defn clean-and-fetch []
  (reset! data* nil)
  (fetch))

(defn form [action]
  [:> Form {:id "mail-template-form"
            :on-submit (fn [e] (.preventDefault e) (action))}
   [:> Form.Group {:control-id "body"}
    [:> Form.Label "Mail Body"]
    [:textarea.form-control
     {:id "body"
      :rows 30
      :required true
      :value (or (:body @data*) "")
      :onChange (fn [e] (swap! data* assoc :body (-> e .-target .-value)))}]]])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.mail-template-debug
     [:hr]
     [:div.mail-template-data
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))
