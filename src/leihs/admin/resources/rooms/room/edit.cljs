(ns leihs.admin.resources.rooms.room.edit
  (:refer-clojure :exclude [str keyword])
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.buildings.building.core :as building-core]
   [leihs.admin.resources.rooms.room.core :as room-core]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom {}))

(defn patch []
  (let [route (path :room {:room-id @room-core/id*})]
    (go (when (some->
               {:url route
                :method :patch
                :json-params  @data*
                :chan (async/chan)}
               http-client/request :chan <!
               http-client/filter-success!)
          (accountant/navigate! route)))))

(defn dialog [& {:keys [show onHide]
                 :or {show false}}]
  (reset! data* @room-core/data*)
  [:> Modal {:size "md"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Edit Room"]]
   [:> Modal.Body
    [room-core/room-form patch]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick onHide}
     "Cancel"]
    [:> Button {:onClick #(do
                            (onHide)
                            (patch))}
     "Save"]]])

