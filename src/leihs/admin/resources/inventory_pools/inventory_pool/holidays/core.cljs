(ns leihs.admin.resources.inventory-pools.inventory-pool.holidays.core
  (:require
   [cljs.core.async :as async :refer [go <!]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

(defonce data* (reagent/atom nil))

(defn fetch []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :inventory-pool-holidays
                                   (-> @routing/state* :route-params))} {} :chan ch)
    (go (let [resp (<! ch)]
          (when (:success resp)
            (reset! data* (-> resp :body)))))))
