(ns leihs.admin.resources.settings.shared.components
  (:require [camel-snake-kebab.core :as csk]))

(defn row [label col-name data]
  (let [class-name (csk/->kebab-case col-name)]
    [:tr {:class class-name}
     [:td [:strong label] [:small (str " (" (name col-name) ")")]]
     [:td {:class class-name} (str (col-name data))]]))

(comment (csk/->kebab-case :lending_terms_acceptance_required_for_order))
