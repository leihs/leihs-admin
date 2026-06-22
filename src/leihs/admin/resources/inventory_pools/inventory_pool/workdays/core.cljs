(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.core
  (:require
   [cljs.core.async :as async :refer [go <! timeout]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

(defonce DAYS {:monday :1
               :tuesday :2
               :wednesday :3
               :thursday :4
               :friday :5
               :saturday :6
               :sunday :0})

(defonce data* (reagent/atom nil))

(defn fetch []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :inventory-pool-workdays
                                   (-> @routing/state* :route-params))} {} :chan ch)
    (go (let [resp (<! ch)]
          (when (:success resp)
            (reset! data* (-> resp :body)))))))

