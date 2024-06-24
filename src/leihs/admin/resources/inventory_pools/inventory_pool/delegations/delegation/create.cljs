(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.create
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.shared :as shared :refer [set-user-id-from-params]]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :as reagent :refer [reaction]]
   [taoensso.timbre]))

(def open*
  (reaction
   (->> (:query-params @routing/state*)
        :action
        (= "add"))))

(defn create [data]
  (go (when-let [id (some->
                     {:chan (async/chan)
                      :url (path :inventory-pool-delegations
                                 {:inventory-pool-id @inventory-pool/id*})
                      :method :post
                      :json-params data}
                     http-client/request :chan <!
                     http-client/filter-success! :body :id)]
        (search-params/delete-from-url "action")
        (accountant/navigate!
         (path :inventory-pool-delegation {:inventory-pool-id @inventory-pool/id*
                                           :delegation-id id})))))

(defn dialog []
  [:> Modal {:size "lg"
             :centered true
             :scrollable true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :onHide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Add a new Delegation"]]
   [:> Modal.Body
    [shared/delegation-form {:action create
                             :id "add-delegation-form"}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-delegation-form"}
     "Add"]]])
