(ns leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.core
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.mail-templates.mail-template.core :as global-core]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Col Form Row]]
   [reagent.core :as reagent]
   [reagent.ratom :as ratom :refer [reaction]]))

(defonce id*
  (reaction (or (-> @routing/state* :route-params :mail-template-id presence)
                ":mail-template-id")))

(defonce pool-id*
  (reaction (or (-> @routing/state* :route-params :inventory-pool-id presence)
                ":inventory-pool-id")))

(defonce path*
  (reaction
   (path :inventory-pool-mail-template
         {:mail-template-id @id*, :inventory-pool-id @pool-id*})))

(defonce cache* (reagent/atom nil))
(defonce data*
  (reaction (get @cache* @path*)))

(defn fetch []
  (http-client/route-cached-fetch cache* {:route @path*}))

(defn form [action data*]
  [:> Row
   [:> Col {:md 12 :lg 8}
    [:> Form {:id "mail-template-form"
              :on-submit (fn [e] (.preventDefault e) (action))}
     [:> Form.Group {:control-id "subject"}
      [:> Form.Label [:strong "Subject"]]
      [:input.form-control
       {:id "subject"
        :required true
        :value (or (:subject @data*) "")
        :onChange (fn [e] (swap! data* assoc :subject (-> e .-target .-value)))}]]
     [:> Form.Group {:control-id "body"}
      [:> Form.Label [:strong "Body"]]
      [:textarea.form-control
       {:id "body"
        :rows 30
        :required true
        :value (or (:body @data*) "")
        :onChange (fn [e] (swap! data* assoc :body (-> e .-target .-value)))}]]]]
   [:> Col
    [:div [:strong "Body variables"]
     (global-core/template-variables)]]])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.mail-template-debug
     [:hr]
     [:div.mail-template-data
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))
