(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.core :as delegation]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.suspension.main :as suspension]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.main :as users]
   [leihs.admin.utils.misc :refer [humanize-datetime-component wait-component]]
   [leihs.core.user.front]
   [react-bootstrap :as react-bootstrap :refer [Table]]))

;;; suspension ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn suspension-section []
  [:section#suspension
   [:h2 " Suspension "]
   [suspension/delegation-page-suspension-component]])

;;; show ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delegation-info-section []
  [:section.delegation
   (if-let [delegation (get @delegation/data* @delegation/id*)]
     [:div
      [:> Table {:striped true :hover true :borderless true}
       [:thead
        [:tr
         [:th "Property"]
         [:th "Value"]]]
       [:tbody
        [:tr.name [:td "Name"] [:td.name (:name delegation)]]
        [:tr.responsible-user
         [:td "Responsible user"]
         [:td.responsible-user
          [users/user-inner-component
           (:responsible_user delegation)]]]
        [:tr.users-count
         [:td "Number of users"]
         [:td.users-count (:users_count delegation)]]
        [:tr.direct-users-count
         [:td "Number of direct users"]
         [:td.direct-users-count (:direct_users_count delegation)]]
        [:tr.groups-count
         [:td "Number of groups"]
         [:td.groups-count (:groups_count delegation)]]
        [:tr.protected
         [:td "Protected"]
         [:td (if (:pool_protected delegation)
                [:span.text-success "yes"]
                [:span.text-warning "no"])]]
        [:tr.contracts-count-open-per-pool
         [:td "Number of contracts open in pool "]
         [:td.contracts-count-open-per-pool (:contracts_count_open_per_pool delegation)]]
        [:tr.contracts-count-per-pool
         [:td "Number of contracts in pool "]
         [:td.contracts-count-per-pool (:contracts_count_per_pool delegation)]]
        [:tr.contracts-count
         [:td "Number of contracts total "]
         [:td.contracts-count (:contracts_count delegation)]]
        [:tr.other-pools
         [:td "Used in the following other pools"]
         [:td [:ul
               (doall (for [pool (:other_pools delegation)]
                        (let [inner [:span "in " (:name pool)]]
                          (if-not (:is_admin  @leihs.core.user.front/state*)
                            [:span inner]
                            [:li {:key (:id pool)}
                             [:a {:href (path :inventory-pool-delegation
                                              {:inventory-pool-id (:id pool)
                                               :delegation-id (:id delegation)})}
                              inner]]))))]]]
        [:tr.created
         [:td "Created "]
         [:td.created (-> delegation :created_at humanize-datetime-component)]]]]]
     [wait-component])])

;; (defn breadcrumbs []
;;   (breadcrumbs/nav-component
;;    @breadcrumbs/left*
;;    [[breadcrumbs/users-li]
;;     [breadcrumbs/groups-li]
;;     [breadcrumbs/edit-li]
;;     [breadcrumbs/suspension-li]]))

(defn page []
  [:article.delegation.my-5
   [delegation/header]
   [delegation/tabs]
   [delegation-info-section]
   [:div.row
    [:div.col-md-6
     [:hr] [suspension-section]]]
   [delegation/debug-component]])
