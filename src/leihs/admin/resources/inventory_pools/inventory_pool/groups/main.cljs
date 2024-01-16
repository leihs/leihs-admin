(ns leihs.admin.resources.inventory-pools.inventory-pool.groups.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.pagination :as pagination :refer [pagination]]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.common.roles.components :refer [put-roles< roles-component]]
   [leihs.admin.common.roles.core :as roles]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.main :as groups]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.nav :as nav]
   [leihs.admin.state :as state]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap]))

;### roles ####################################################################

(defn roles-update-handler [roles group]
  (go (swap! groups/data* assoc-in
             [(:route @routing/state*) :groups (:page-index group) :roles]
             (<! (put-roles<
                  (path :inventory-pool-group-roles
                        {:inventory-pool-id @inventory-pool/id*
                         :group-id (:id group)})
                  roles)))))

(defn roles-th-component  []
  [:th.pl-5 {:key :roles} " Roles "])

(defn roles-td-component [group]
  [:td.pl-5 {:key :roles}
   [roles-component
    (get group :roles)
    :compact true
    :update-handler #(roles-update-handler % group)

    :label "Role"
    :query-params-key :role
    :default-option "customer"]])

;### actions ##################################################################

(defn form-role-filter []
  [routing/select-component
   :label "Role"
   :query-params-key :role
   :default-option "customer"
   :options (merge {"" "(any role or none)"
                    "none" "none"}
                   (->> roles/hierarchy
                        (map (fn [%1] [%1 %1]))
                        (into {})))])

(defn filter-section []
  [filter/container
   [:<>
    [filter/form-term]
    [filter/form-including-user]
    [form-role-filter]
    [filter/form-per-page]
    [filter/reset]]])

;### main #####################################################################

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div]))

;; TODO: needs to be added
(defn add-group-button []
  [:> react-bootstrap/Button
   {:href (path :inventory-pool-user-create {:inventory-pool-id @inventory-pool/id*})
    :variant "primary"
    :className "ml-4"}
   [:span [icons/add] " Add Group"]])

(defn table-toolbar []
  [:> react-bootstrap/ButtonToolbar {:className "my-3"}
   [pagination]
   [add-group-button]])

(defn main-page-component []
  [:<>
   [routing/hidden-state-component
    {:did-change groups/fetch-groups}]
   [filter-section]
   [table-toolbar]
   [groups/table-component
    [groups/name-th-component groups/users-count-th-component roles-th-component]
    [groups/name-td-component groups/users-count-td-component roles-td-component]]
   [table-toolbar]
   [debug-component]
   [groups/debug-component]])

(defn page []
  [:div.inventory-pool-groups
   [routing/hidden-state-component
    {:did-mount (fn [_] (inventory-pool/clean-and-fetch))}]
   [:h1.my-5
    [inventory-pool/name-component]]
   [nav/tabs]
   [:<>
    [main-page-component]]])
