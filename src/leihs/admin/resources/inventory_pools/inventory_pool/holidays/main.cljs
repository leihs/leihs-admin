(ns leihs.admin.resources.inventory-pools.inventory-pool.holidays.main
  (:require
   [clojure.string :refer [capitalize]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.inventory-pool.holidays.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.holidays.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.front.debug :refer [spy]]
   [leihs.core.routing.front :as routing]))

(defn table-body []
  (let [data @core/data*]
    (fn []
      [:<>
       (doall (for [holiday data]
                [:tr {:key (:id holiday)}
                 [:td (:name holiday)]
                 [:td (:start_date holiday)]
                 [:td (:end_date holiday)]]))])))

(defn component []
  [:<>
   [routing/hidden-state-component
    {:did-change core/clean-and-fetch}]

   (if-not @core/data*
     [wait-component]
     [:div#holidays
      [:h3 "Holidays"]
      [table/container
       {:borders false
        :header [:tr [:th "Name"] [:th "From"] [:th "To"]]
        :body [table-body]}]
      [edit/button]])])
