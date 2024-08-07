(ns leihs.admin.resources.users.user.inventory-pools
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [cljs.pprint :refer [pprint]]
   [clojure.string :refer [join]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.roles.core :as roles]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.core :as pool-core]
   [leihs.admin.resources.users.user.core :as user-core :refer [user-data*
                                                                user-id*]]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :as front-shared :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn fetch []
  (go (reset! data*
              (some->
               {:chan (async/chan)
                :url (path :user-inventory-pools
                           (-> @routing/state* :route-params))}
               http-client/request :chan <!
               http-client/filter-success!
               :body :user-inventory-pools))))

(defn clean-and-fetch []
  (reset! data* nil)
  (fetch))

(defn debug-component []
  [:div
   (when (:debug @state/global-state*)
     [:div.inventory-pools-debug
      [:hr]
      [:div.inventory-pools-data
       [:h3 "@data*"]
       [:pre (with-out-str (pprint @data*))]]])])

(defn user-in-pool-td-component [row]
  (let [data @user-data*]
    (fn []
      (let [inventory-pool-id (:inventory_pool_id row)
            has-access? (pool-auth/current-user-is-some-manager-of-pool? inventory-pool-id)
            pool-path (path :inventory-pool
                            {:inventory-pool-id (:inventory_pool_id row)})
            user-in-pool-path (path :inventory-pool-user
                                    {:inventory-pool-id (:inventory_pool_id row)
                                     :user-id @user-id*})
            user-in-pool-inner [:<> (user-core/fullname-or-some-uid data)]
            pool-inner [:em (:inventory_pool_name row)]]
        [:td
         [:span
          (if has-access?
            [:a {:href user-in-pool-path} user-in-pool-inner]
            user-in-pool-inner)
          " in "
          (if has-access?
            [:a {:href pool-path} pool-inner]
            pool-inner)]]))))

(defn roles-td-component [row]
  [:td
   (->> row :role roles/expand-to-hierarchy
        (map name)
        (join ", "))])

(defn contracts-td-component [row]
  [:td
   (:open_contracts_count row)
   " / "
   (:contracts_count row)])

(defn reservations-td-component [row]
  [:td
   (:submitted_reservations_count row)
   " ; "
   (:approved_reservations_count row)
   " / "
   (:reservations_count row)])

(defn table-component [& {:keys [chrome]
                          :or {chrome true}}]
  [:div.user-inventory-pools
   [routing/hidden-state-component
    {:did-mount #(fetch)}]

   (if (and @data* @user-data*)
     [table/container
      {:borders chrome
       :header [:tr
                [:th "User in pool"]
                [:th "Roles"]
                [:th "Submitted ; approved / total reservations"]
                [:th "Open / total contracts"]]
       :body (for [row (->>  @data* (sort-by :inventory_pool_name))]
               [:tr.pool {:key (:inventory_pool_id row)}
                [user-in-pool-td-component row]
                [roles-td-component row]
                [reservations-td-component row]
                [contracts-td-component row]])}]
     [wait-component])
   [debug-component]])
