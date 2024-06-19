(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.edit
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.core :as delegation]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.shared :as shared :refer [set-user-id-from-params]]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [taoensso.timbre]))

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
          (delegation/fetch-delegation)
          (accountant/navigate! route)))))

(defn dialog [& {:keys [show onHide]
                 :or {show false}}]
  [:> Modal {:size "lg"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Edit Delegation"]]
   [:> Modal.Body
    [shared/delegation-form {:action patch
                             :id "add-delegation-form"}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick onHide}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-delegation-form"
                :on-click #(do (js/console.debug "on click action" @@shared/user-delegation* @shared/data*)
                               (patch @(set-user-id-from-params)))}
     "Save"]]])
