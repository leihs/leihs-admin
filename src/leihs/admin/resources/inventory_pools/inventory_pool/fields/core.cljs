(ns leihs.admin.resources.inventory-pools.inventory-pool.fields.core
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn fetch []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :inventory-pool-fields (-> @routing/state* :route-params))} {} :chan ch)
    (go (let [resp (<! ch)]
          (when (:success resp)
            (reset! data* (-> resp :body :fields)))))))

(defn clean-and-fetch []
  (reset! data* nil)
  (fetch))

; helper

(defn format-target-type [target-type]
  (case target-type
    "item" "Item"
    "license" "License"
    nil "Item+License"
    "n/a"))
