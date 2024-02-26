(ns leihs.admin.resources.groups.group.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.admin.common.components.navigation.back :as back]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.group.core :refer [clean-and-fetch data*
                                                    debug-component
                                                    group-name-component]]
   [leihs.admin.resources.groups.group.delete :as delete]
   [leihs.admin.resources.groups.group.edit :as edit]
   [leihs.admin.resources.groups.group.inventory-pools :as inventory-pools]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Nav]]
   [reagent.core :as reagent]))

(defn li-dl-component [dt dd]

  ^{:key dt}
  [:li {:key dt}
   [:dl.row.mb-0
    [:dt.col-sm-4 dt]
    [:dd.col-sm-8 dd]]])

(defn edit-group-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:onClick #(reset! show true)}
        "Edit Group"]
       [edit/dialog {:show @show
                     :onHide #(reset! show false)}]])))

(defn delete-group-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:variant "danger"
         :className "ml-3"
         :onClick #(reset! show true)}
        "Delete Group"]
       [delete/dialog  {:show @show
                        :onHide #(reset! show false)}]])))

(defn properties-component []
  [:div
   (if-not @data*
     [wait-component]
     [:<>
      [:ul.list-unstyled
       [li-dl-component "Name" (:name @data*)]
       [li-dl-component "Description " (:description @data*)]
       [li-dl-component "Admin protected"
        (if (:admin_protected @data*) "yes" "no")]
       [li-dl-component "System-admin protected"
        (if (:system_admin_protected @data*) "yes" "no")]
       [li-dl-component "Organization" (:organization @data*)]
       [li-dl-component "Org ID" (:org_id @data*)]
       [li-dl-component "Number of users" (:users_count @data*)]]
      [:div.mt-3
       [edit-group-button]
       [delete-group-button]]])])

(defn page []
  [:article.group
   [routing/hidden-state-component
    {:did-mount clean-and-fetch
     :did-change clean-and-fetch}]
   [:header.my-5
    [back/button  {:href (path :groups {})}]
    [:h1.mt-3 [group-name-component]]]
   [properties-component]

   [:> Nav {:variant "tabs" :className "mt-5"
            :defaultActiveKey "users"}
    [:> Nav.Item
     [:> Nav.Link {:active true} "Inventory Pools"]]
    [:> Nav.Item
     [:> Nav.Link
      {:href (-> (:path @routing/state*)
                 (clojure.core/str "/users/"))}
      "Users"]]]

   [:div
    [inventory-pools/table-component]]
   [debug-component]])
