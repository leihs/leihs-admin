(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.main
  (:require
   [clojure.string :refer [capitalize]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.front.debug :refer [spy]]
   [leihs.core.routing.front :as routing]))

(defn component []
  [:<>
   [routing/hidden-state-component
    {:did-mount core/clean-and-fetch}]
   (if-not @core/data*
     [wait-component]
     [:div
      [table/container
       {:borders false
        :header [:tr [:th "Day"] [:th "Opened"] [:th "Max. Allowed Visits"]]
        :body (doall (for [day (keys core/DAYS)]
                       [:tr {:key day}
                        [:td (capitalize (name day))]
                        [:td (str (day @core/data*))]
                        [:td (or ((core/DAYS day) (@core/data* :max_visits))
                                 "unlimited")]]))}]
      [edit/button]])])