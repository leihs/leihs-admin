(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.main
  (:require
   [clojure.string :refer [capitalize]]
   [leihs.admin.common.components :refer [toggle-component]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn component []
  [:<>
   [routing/hidden-state-component
    {:did-mount core/fetch}]

   (if-not @core/data*
     [wait-component]
     [:div#workdays
      [:h3 "Workdays"]
      [table/container
       {:borders false
        :style {:width "100%"}
        :header [:tr
                 [:th "Day"]
                 [:th "Opened"]
                 [:th "Orders processed *"]
                 [:th {:style {:width "40%"}} "Hours Info"]
                 [:th "Max Visits"]]
        :body (doall (for [day (keys core/DAYS)]
                       [:tr {:key day}
                        [:td (capitalize (name day))]
                        [:td (toggle-component (day @core/data*))]
                        [:td (toggle-component ((-> (name day) (str "_orders_processing") keyword)
                                                @core/data*))]
                        (let [day-info-key (-> day name (str "_info") keyword)]
                          [:td {:id day-info-key} (day-info-key @core/data*)])
                        [:td (or ((core/DAYS day) (@core/data* :max_visits))
                                 "unlimited")]]))}]
      [:div.mb-3 [:i "* If activated, orders can be processed on a particular day."]]
      [edit/button]
      [edit/dialog]])])
