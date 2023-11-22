(ns leihs.admin.resources.rooms.room.delete
  (:refer-clojure :exclude [str keyword])
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]))

(defn post []
  (go (when (some->
             {:url (path :room (-> @routing/state* :route-params))
              :method :delete
              :chan (async/chan)}
             http-client/request :chan <!
             http-client/filter-success!)
        (accountant/navigate! (path :rooms)))))

(defn dialog [& {:keys [show onHide]
                 :or {show false}}]
  [:> Modal {:size "sm"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Delete Room"]]
   [:> Modal.Body
    "Are you sure you want to delete this Room? This action cannot be undone."]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick onHide}
     "Cancel"]
    [:> Button {:variant "danger"
                :onClick #(do
                            (onHide)
                            (post))}
     "Delete"]]])

