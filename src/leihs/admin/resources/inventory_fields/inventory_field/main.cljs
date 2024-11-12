(ns leihs.admin.resources.inventory-fields.inventory-field.main
  (:require
   [leihs.admin.common.components.navigation.breadcrumbs :as breadcrumbs]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-fields.inventory-field.core :as core]
   [leihs.admin.resources.inventory-fields.inventory-field.delete :as delete]
   [leihs.admin.resources.inventory-fields.inventory-field.edit :as edit]
   [leihs.admin.resources.inventory-fields.inventory-field.inventory-pools.main :as inventory-pools]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn info-table []
  [:<>
   [table/container
    {:borders false
     :header [:tr [:th "Property"] [:th.w-75 "Value"]]
     :body
     [:<>
      [:tr.active
       [:td [:strong "Active"] [:small " (active)"]]
       [:td.active (str (:active @core/inventory-field-data*))]]
      [:tr.label
       [:td [:strong "Label"] [:small " (data:label)"]]
       [:td.label (-> @core/inventory-field-data* :data :label)]]
      [:tr.dynamic
       [:td [:strong "Configurable"] [:small " (dynamic)"]]
       [:td.dynamic (str (:dynamic @core/inventory-field-data*))]]
      [:tr.required
       [:td [:strong "Required"] [:small " (data:required)"]]
       [:td.required (str (or (-> @core/inventory-field-data* :data :required) false))]]
      [:tr.attribute
       [:td [:strong "Unique ID-Attribute"] [:small " (data:attribute)"]]
       [:td.attribute (str (-> @core/inventory-field-data* :data :attribute))]]
      [:tr.forPackage
       [:td [:strong "Enabled for packages"] [:small " (data:forPackage)"]]
       [:td.forPackage (str (or (-> @core/inventory-field-data* :data :forPackage) false))]]
      [:tr.owner
       [:td [:strong "Editable by owner only"] [:small " (data:permissions:owner)"]]
       [:td.owner (str (or (-> @core/inventory-field-data* :data :permissions :owner) false))]]
      [:tr.role
       [:td [:strong "Minimum role required for view"] [:small " (data:permissions:role)"]]
       [:td.role (or (-> @core/inventory-field-data* :data :permissions :role) "inventory_manager")]]
      [:tr.field-group
       [:td [:strong "Field Group"] [:small " (data:group)"]]
       [:td.field-group (or (-> @core/inventory-field-data* :data :group) "None")]]
      [:tr.target-type
       [:td [:strong "Target"] [:small " (data:target_type)"]]
       [:td.target-type (or (-> @core/inventory-field-data* :data :target_type) "License+Item")]]
      [:tr.type
       [:td [:strong "Type"] [:small " (data:type)"]]
       [:td.type (-> @core/inventory-field-data* :data :type)]]]}]])

(defn header []
  [:header.my-5
   [breadcrumbs/main  {:to (path :inventory-fields)}]
   [:h1.mt-3 (-> @core/inventory-field-data* :data :label)]
   [:p "( " (:id @core/inventory-field-data*) " )"]])

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount (fn []
                  (core/fetch-inventory-fields-groups)
                  (core/fetch))}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]

     [:article.room
      [header]
      [:section
       [info-table]
       [edit/button]
       [edit/dialog]
       [delete/button]
       [delete/dialog]
       [:div.mt-5.mb-5
        [inventory-pools/component]]
       [core/debug-component]]])])
