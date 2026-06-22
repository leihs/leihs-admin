(ns leihs.admin.resources.mail-templates.mail-template.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.mail-templates.mail-template.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(def data* (reagent/atom nil))

(defn patch []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :mail-template {:mail-template-id @core/id*})
                        :method :patch
                        :json-params @data*}
                       {}
                       :chan ch)
    (go (when (:success (<! ch))
          (swap! core/cache* assoc @core/path* @data*)
          (search-params/delete-from-url "action")))))

(def open?*
  (reaction
   (reset! data* @core/data*)
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn dialog [& {:keys [show onHide]
                 :or {show false}}]
  [:> Modal {:size "xl"
             :centered true
             :scrollable true
             :show @open?*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url
                                "action")}
    [:> Modal.Title "Edit Mail Template"]]
   [:> Modal.Body
    [core/form patch data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url
                            "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "mail-template-form"}
     "Save"]]])

(defn button []
  [:> Button
   {:on-click #(search-params/append-to-url
                {:action "edit"})}
   "Edit"])
