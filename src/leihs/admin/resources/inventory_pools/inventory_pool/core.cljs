(ns leihs.admin.resources.inventory-pools.inventory-pool.core
  (:require
   [cljs.core.async :as async :refer [go <!]]
   [cljs.pprint :refer [pprint]]
   [clojure.string :refer [join]]
   [leihs.admin.common.components :as components]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as workdays]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.auth.core :as auth]
   [leihs.core.core :refer [presence]]
   [leihs.core.front.debug :refer [spy]]
   [leihs.core.routing.front :as routing]
   [leihs.core.user.front :as current-user]
   [leihs.core.user.shared :refer [short-id]]
   [react-bootstrap :as react-bootstrap]
   [reagent.core :as reagent :refer [reaction]]))

(defonce id*
  (reaction (or (-> @routing/state* :route-params :inventory-pool-id presence)
                ":inventory-pool-id")))

(defonce data* (reagent/atom nil))

;;; fetch ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch []
  (go (reset! data*
              (some->
               {:chan (async/chan)
                :url (path :inventory-pool
                           (-> @routing/state* :route-params))}
               http-client/request :chan <!
               http-client/filter-success! :body))))

(defn clean-and-fetch [& args]
  (reset! data* nil)
  (fetch))

;;; debug ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.inventory-pool-debug
     [:hr]
     [:div.inventory-pool-data
      [:h3 "@data*"]
      [:pre (with-out-str (pprint @data*))]]
     [:div.inventory-pool-workdays-data
      [:h3 "@workdays/data*"]
      [:pre (with-out-str (pprint @workdays/data*))]]]))

;;; components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn inventory-pool-form [& {:keys [is-editing]
                              :or {is-editing false}}]
  (if-not @data*
    [wait-component]
    [:div.inventory-pool.mt-3
     [:div.mb-3
      [form-components/switch-component data* [:is_active]
       :disabled (not @current-user/admin?*)
       :label "Active"]]
     [:div
      [form-components/input-component data* [:name]
       :label "Name"
       :required true]]
     [:div
      [form-components/input-component data* [:shortname]
       :label "Short name"
       :disabled is-editing
       :required true]]
     [:div
      [form-components/input-component data* [:email]
       :label "Email"
       :type :email
       :required true]]
     [form-components/input-component data* [:description]
      :label "Description"
      :element :textarea
      :rows 10]
     (when (and is-editing
                (auth/allowed? [pool-auth/pool-inventory-manager?
                                auth/admin-scopes?]))
       [:<>
        [form-components/input-component data* [:default_contract_note]
         :label "Default Contract Note"
         :element :textarea
         :rows 5]
        [:div.mb-3
         [form-components/switch-component data* [:print_contracts]
          :label "Print Contracts"]]
        [:div.mb-3
         [form-components/switch-component data* [:automatic_suspension]
          :label "Automatic Suspension"]]
        [form-components/input-component data* [:automatic_suspension_reason]
         :label "Automatic Suspension Reason"
         :element :textarea
         :rows 5]
        [form-components/switch-component data* [:required_purpose]
         :label "Hand Over Purpose"]])]))

;; shared tabs for main view
(defn tabs [active]
  [:> react-bootstrap/Nav {:className "mb-3"
                           :justify false
                           :variant "tabs"
                           :defaultActiveKey active}
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     {:href (join ["/admin/inventory-pools/" @id*])}
     [icons/inventory-pools]
     " Settings "]]
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     (let [href (path :inventory-pool-users
                      {:inventory-pool-id @id*})]
       {:active (clojure.string/includes? (:path @routing/state*) href)
        :href href})
     [icons/users]
     " Users "]]
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     (let [href (path :inventory-pool-groups
                      {:inventory-pool-id @id*})]
       {:active (clojure.string/includes? (:path @routing/state*) href)
        :href href})
     [icons/groups]
     " Groups "]]
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     (let [href (path :inventory-pool-delegations
                      {:inventory-pool-id @id*})]
       {:active (clojure.string/includes? (:path @routing/state*) href)
        :href href})
     [icons/delegations]
     " Delegations "]]
   [:> react-bootstrap/Nav.Item
    [:> react-bootstrap/Nav.Link
     (let [href (path :inventory-pool-entitlement-groups
                      {:inventory-pool-id @id*})]
       {:active (clojure.string/includes? (:path @routing/state*) href)
        :href href})
     [icons/award]
     " Entitlement-Groups "]]])

(defn name-component []
  [:<>
   [routing/hidden-state-component
    {:did-change fetch}]
   (let [inner (when @data*
                 [:<> (str (:name @data*))])]
     [:<> inner])])

(defn header []
  [:header.my-5
   [:h1.mt-3 [name-component]]])

(defn name-link-component []
  [:span
   [routing/hidden-state-component
    {:did-change fetch}]
   (let [p (path :inventory-pool {:inventory-pool-id @id*})
         inner (if @data*
                 [:em (str (:name @data*))]
                 [:span {:style {:font-family "monospace"}} (short-id @id*)])]
     [components/link inner p])])
