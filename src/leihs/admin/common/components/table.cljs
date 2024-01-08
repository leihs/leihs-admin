(ns leihs.admin.common.components.table
  (:require
   [leihs.admin.common.components.pagination :refer [pagination]]
   [react-bootstrap :as react-bootstrap]))

(defn table-toolbar [items]
  [:> react-bootstrap/ButtonToolbar {:className "my-3"}
   [pagination]
   items])

(defn table [header body footer]
  [:<>
   [:hr]
   [:> react-bootstrap/Table {:striped true :hover true :borderless true}
    [:thead
     header]
    [:tbody
     body]
    [:footer
     footer]]
   [:hr]])

