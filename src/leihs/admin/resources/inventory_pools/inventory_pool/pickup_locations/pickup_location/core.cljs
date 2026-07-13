(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.pickup-location.core
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.state :as state]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

(defonce id*
  (reaction (or (-> @routing/state* :route-params :pickup-location-id presence)
                ":pickup-location-id")))

(defonce pool-id*
  (reaction (or (-> @routing/state* :route-params :inventory-pool-id presence)
                ":inventory-pool-id")))

(defonce path*
  (reaction
   (path :inventory-pool-pickup-location
         {:inventory-pool-id @pool-id*, :pickup-location-id @id*})))

(defonce cache* (reagent/atom nil))

(defonce data*
  (reaction (get @cache* @path*)))

(defn fetch []
  (http-client/route-cached-fetch cache* {:route @path*}))

(defn pickup-location-form [data*]
  [:div
   [form-components/input-component data* [:name]
    :label "Name"
    :required true]
   [form-components/input-component data* [:description]
    :label "Description"
    :element :textarea
    :rows 5]])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.pickup-location-debug
     [:hr]
     [:div.pickup-location-data
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]]))
