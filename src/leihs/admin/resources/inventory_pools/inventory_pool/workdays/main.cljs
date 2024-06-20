(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.main
  (:require
   [clojure.string :refer [capitalize]]
   [leihs.admin.common.components :refer [toggle-component]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn table-body []
  (let [data @core/data*]
    (fn []
      [:<>
       (doall (for [day (keys core/DAYS)]
                [:tr {:key day}
                 [:td (capitalize (name day))]
                 [:td (toggle-component (day data))]
                 [:td (or ((core/DAYS day) (data :max_visits))
                          "unlimited")]]))])))

(defn component []
  [:<>
   [routing/hidden-state-component
    {:did-change core/clean-and-fetch}]

   (if-not @core/data*
     [wait-component]
     [:div#workdays
      [:h3 "Workdays"]
      [table/container
       {:borders false
        :header [:tr [:th "Day"] [:th "Opened"] [:th "Max. Allowed Visits"]]
        :body [table-body]}]
      [edit/button]])])
