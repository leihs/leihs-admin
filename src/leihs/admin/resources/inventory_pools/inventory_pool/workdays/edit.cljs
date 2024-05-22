(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.edit
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [go <!]]
   [clojure.string :refer [capitalize]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as core]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.auth.core :as auth]
   [leihs.core.constants :as constants]
   [react-bootstrap :as BS :refer [Button Modal]]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn patch []
  (let [route (path :inventory-pool-workdays
                    {:inventory-pool-id (:inventory_pool_id @data*)})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params @data*
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
          (accountant/navigate! route)))))

(defn opened-closed-comp [day]
  (let [switch-id (str (name day) "-switch")]
    [:div.custom-control.custom-switch
     [:input.custom-control-input
      {:id switch-id
       :name (name day)
       :type :checkbox
       :checked (day @data*)
       :on-change #(swap! data* update day not)
       :tab-index constants/TAB-INDEX}]
     [:label.custom-control-label {:for switch-id}]]))

(defn form []
  (if-not @data*
    [wait-component]
    [table/container
     {:borders false
      :header [:tr [:th "Day"] [:th "Open/Closed"] [:th "Max. Allowed Visits"]]
      :body (doall (for [day (keys core/DAYS)]
                     [:tr {:key (name day)}
                      [:td (capitalize (name day))]
                      [:td [opened-closed-comp day data*]]
                      [:td ((core/DAYS day) (:max_visits @data*))]]))}]))

(defn dialog [& {:keys [show onHide] :or {show false}}]
  (fn [& {:keys [show onHide] :or {show false}}]
    [:> Modal {:size "lg"
               :centered true
               :show show}
     [:> Modal.Header {:closeButton true
                       :onHide onHide}
      [:> Modal.Title "Edit Workdays"]]
     [:> Modal.Body [form]]
     [:> Modal.Footer
      [:> Button {:variant "secondary" :onClick onHide}
       "Cancel"]
      [:> Button {:onClick #(do (patch) (onHide))}
       "Save"]]]))

(defn button []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    (let [show (reagent/atom false)]
      (fn []
        [:<>
         [:> Button
          {:onClick #(do (when-not @data* (reset! data* @core/data*))
                         (reset! show true))}
          "Edit"]
         [dialog {:show @show
                  :onHide #(reset! show false)}]]))))
