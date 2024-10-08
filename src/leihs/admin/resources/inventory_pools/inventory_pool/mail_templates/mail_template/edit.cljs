(ns leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(def data* (reagent/atom nil))

(defn patch []
  (let [route (path :inventory-pool-mail-template
                    {:mail-template-id @core/id*,
                     :inventory-pool-id @core/pool-id*})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params  @data*
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
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
