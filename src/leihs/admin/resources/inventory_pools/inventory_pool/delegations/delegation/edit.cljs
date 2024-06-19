(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.core :as delegation]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]
   [taoensso.timbre]))

(def open*
  (reaction
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn patch [data]
  (let [route (path :inventory-pool-delegation
                    {:inventory-pool-id @inventory-pool/id*
                     :delegation-id @delegation/id*})]
    (go (when (some->
               {:chan (async/chan)
                :url route
                :method :patch
                :json-params  data}
               http-client/request :chan <!
               http-client/filter-success!)
          (search-params/delete-all-from-url)))))

(defn dialog []
  [:> Modal {:size "lg"
             :centered true
             :scrollable true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :onHide #(search-params/delete-all-from-url)}
    [:> Modal.Title "Edit Delegation"]]
   [:> Modal.Body
    [delegation/delegation-form {:action patch
                                 :id "add-delegation-form"}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-all-from-url)}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-delegation-form"}
     "Save"]]])
