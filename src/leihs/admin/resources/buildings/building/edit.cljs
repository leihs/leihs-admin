(ns leihs.admin.resources.buildings.building.edit
  (:refer-clojure :exclude [str keyword])
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.buildings.building.core :as building-core]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom {}))

(defn patch []
  (let [route (path :building {:building-id @building-core/id*})]
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
  (reset! data* @building-core/data*)
  [:> Modal {:size "md"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Edit Building"]]
   [:> Modal.Body
    [building-core/building-form data*]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick onHide}
     "Cancel"]
    [:> Button {:onClick #(do
                            (onHide)
                            (patch))}
     "Save"]]])

