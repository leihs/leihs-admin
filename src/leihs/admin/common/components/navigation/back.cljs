(ns leihs.admin.common.components.navigation.back
  (:require
   [leihs.admin.common.icons :as icons]
   [react-bootstrap :as react-bootstrap]))

(defn back []
  [:> react-bootstrap/Button
   {:variant "outline-primary"
    :onClick #(js/history.back)}
   [icons/back] " Back"])
