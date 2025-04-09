(ns leihs.admin.resources.inventory-pools.inventory-pool.holidays.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [com.rpl.specter :as s]
   [leihs.admin.common.components :refer [toggle-component]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.inventory-pools.inventory-pool.holidays.core :as core]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.constants :as constants]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as BS :refer [Button Form Modal]]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(defn prepare-for-patch [data]
  (->> data
       (map #(if (:new %) (dissoc % :new :id) %))))

(defn patch []
  (let [route (path :inventory-pool-holidays
                    {:inventory-pool-id (:id @pool-core/data*)})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params (prepare-for-patch @data*)
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
          (core/fetch)
          (search-params/delete-from-url "action")))))

(defn to-iso-8601 [date]
  (-> date .toISOString (.split "T") first))

(defn add-new-holiday-comp []
  (let [holiday-template {:inventory_pool_id (:id @pool-core/data*),
                          :orders_processing false}
        new-holiday (reagent/atom holiday-template)
        end-date-before-start-date? (reagent/atom false)]
    (fn []
      (let [today (-> (new js/Date) to-iso-8601)]
        [:<>
         [:form.form-inline.align-items-end {:on-submit (fn [e]
                                                          (.preventDefault e)
                                                          (swap! data* conj
                                                                 (assoc @new-holiday
                                                                        :new true
                                                                        :id (str (random-uuid))))
                                                          (reset! new-holiday holiday-template))}
          [:div.form-group.mb-2.mr-2 {:style {:width "30%"}}

           [:label {:class-name "font-weight-bold mb-2"
                    :for "name"} "Name"]
           [:input.form-control.w-100 {:type "text" :placeholder "Name"
                                       :value (:name @new-holiday)
                                       :required true
                                       :on-change #(swap! new-holiday
                                                          assoc :name
                                                          (-> % .-target .-value))}]]
          [:div.form-group.mb-2.mr-2.flex-column.align-items-start
           {:style {:width "20%"}}

           [:label {:class-name "font-weight-bold mb-2"
                    :for "start-date"} "From"]
           [:input.form-control.w-100 {:type "date" ;:placeholder "From"
                                       :id "start-date"
                                       :value (:start_date @new-holiday)
                                       :min today
                                       :required true
                                       :on-change #(do (swap! new-holiday
                                                              assoc :start_date
                                                              (-> % .-target .-value)))}]]

          [:div.form-group.mb-2.mr-2.flex-column.align-items-start
           {:style {:width "20%"}}

           [:label {:class-name "font-weight-bold mb-2"
                    :for "end-date"} "To"]
           [:input.form-control.w-100 {:type "date" ;:placeholder "To"
                                       :id "end-date"
                                       :value (:end_date @new-holiday)
                                       :min today
                                       :required true
                                       :on-change #(do (swap! new-holiday
                                                              assoc :end_date
                                                              (-> % .-target .-value)))}]]

          [:div.form-group.mb-2.mr-2.flex-column.align-items-start
           {:style {:width "15%"}}

           [:label {:class-name "font-weight-bold mb-4"
                    :for "switch-id"} "Orders processed*"]

           (let [switch-id "new-orders-processed-switch"]
             [:div.custom-control.custom-switch
              [:input.custom-control-input
               {:id switch-id
                :name :orders_processing
                :type :checkbox
                :checked (:orders_processing @new-holiday)
                :on-change #(swap! new-holiday update :orders_processing not)
                :tab-index constants/TAB-INDEX}]
              [:label.custom-control-label {:for switch-id}]])]

          [:> Button {:type "submit"
                      :className "btn-info mb-2 ml-3"
                      :disabled @end-date-before-start-date?}
           "Add"]]]))))

(defn holiday-row-comp [holiday]
  [:<>
   [:td (cond->> (:name holiday) (:delete holiday) (vector :s))]
   [:td (cond->> (:start_date holiday) (:delete holiday) (vector :s))]
   [:td (cond->> (:end_date holiday) (:delete holiday) (vector :s))]
   [:td (toggle-component (:orders_processing holiday))]
   [:td
    (let [specter-path [s/ALL #(= (:id %) (:id holiday))]]
      (if (:delete holiday)
        [:> Button
         {:onClick
          (fn [_]
            (swap! data*
                   (fn [d]
                     (s/transform specter-path #(dissoc % :delete) d))))
          :variant "outline-secondary" :size "sm"}
         "Restore"]
        [:> Button
         {:onClick
          (fn [_]
            (swap! data*
                   (fn [d]
                     (if (:new holiday)
                       (s/setval specter-path s/NONE d)
                       (s/transform specter-path #(assoc % :delete true) d)))))
          :variant "outline-danger" :size "sm"}
         "Delete"]))]])

(defn form []
  (if-not @pool-core/data*
    [wait-component]
    [:<>
     [add-new-holiday-comp]
     [:> Form {:id "holidays-form"
               :on-submit (fn [e]
                            (.preventDefault e)
                            (patch))}
      [table/container
       {:borders false
        :header [:tr
                 [:th {:style {:width "30%"}}]
                 [:th {:style {:width "20%"}}]
                 [:th {:style {:width "20%"}}]
                 [:th {:style {:width "15%"}}]
                 [:th {:style {:width "10%"}}]]
        :body
        (doall (for [holiday @data*]
                 [:tr {:key (:id holiday)
                       :class (when (:delete holiday) "table-danger")}
                  [holiday-row-comp holiday]]))}]]]))

(comment (let [hs [{:id 1} {:id 2}]]
           (s/transform [s/ALL #(= (:id %) 1)]
                        #(assoc % :delete true)
                        hs)))

(def open*
  (reaction
   (reset! data* @core/data*)
   (->> (:query-params @routing/state*)
        :action
        (= "edit-holidays"))))

(defn dialog []
  [:> Modal {:size "lg"
             :scrollable true
             :centered true
             :show @open*}
   [:> Modal.Header {:close-button true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Edit Holidays"]]
   [:> Modal.Body
    [form]
    [:div.mb-3 [:i "* If activated, orders can be processed during a particular holiday."]]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "holidays-form"}
     "Save"]]])

(defn button []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    [:<>
     [:> Button
      {:on-click #(search-params/append-to-url
                   {:action "edit-holidays"})}
      "Edit"]]))
