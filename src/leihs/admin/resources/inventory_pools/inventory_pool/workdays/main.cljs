(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.main
  (:require
   ["react-bootstrap" :refer [Form]]
   [clojure.string :refer [capitalize]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.front.debug :refer [spy]]
   [leihs.core.routing.front :as routing]))

(defn table []
  (let [data @core/data*]
    (fn []
      [table/container
       {:borders false
        :header [:tr [:th "Day"] [:th "Opened"] [:th "Max. Allowed Visits"]]
        :body (doall (for [day (keys core/DAYS)]
                       [:tr {:key day}
                        [:td (capitalize (name day))]
                        [:td [:> Form.Check {:type "switch"
                                             :id (str (name day) "-switch")
                                             :disabled true
                                             :default-checked (day data)}]]
                        [:td (or ((core/DAYS day) (data :max_visits))
                                 "unlimited")]]))}])))

(defn component []
  [:<>
   [routing/hidden-state-component
    {:did-mount core/clean-and-fetch}]
   (if-not @core/data*
     [wait-component]
     [:div
      [:h3 "Workdays"]
      [table]
      [edit/button]])])
