(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.create
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.shared :as shared :refer [set-user-id-from-params]]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]
   [taoensso.timbre]))

(defn create [data]
  (go (when-let [id (some->
                     {:chan (async/chan)
                      :url (path :inventory-pool-delegations
                                 {:inventory-pool-id @core/id*})
                      :method :post
                      :json-params data}
                     http-client/request :chan <!
                     http-client/filter-success! :body :id)]
        (search-params/delete-from-url "action")
        (accountant/navigate!
         (path :inventory-pool-delegation {:inventory-pool-id @core/id*
                                           :delegation-id id})))))

(def open*
  (reaction
   (reset! core/data* nil)
   (->> (:query-params @routing/state*)
        :action
        (= "add"))))

(defn dialog []
  [:> Modal {:size "lg"
             :centered true
             :scrollable true
             :show @open*}
   [:> Modal.Header {:close-button true
                     :on-hide #(search-params/delete-all-from-url)}
    [:> Modal.Title "Add a new Delegation"]]
   [:> Modal.Body
    [shared/delegation-form {:action create
                             :id "add-delegation-form"}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-all-from-url)}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-delegation-form"}
     "Add"]]])
