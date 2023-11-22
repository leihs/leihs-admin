(ns leihs.admin.common.components.pagination
  (:require
   [leihs.core.paths :refer [path]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap]))

(defn pagination []
  (let [hk (some-> @routing/state* :handler-key)
        route-params (or (some-> @routing/state* :route-params) {})
        query-parameters (some-> @routing/state* :query-params-raw)
        current-page (or (some-> query-parameters :page int) 1)]
    (if-not hk
      [:div "pagination not ready"]
      [:div
       ;(console.log 'HK (clj->js hk))
       (let [ppage (dec current-page)
             ppagepath (path hk route-params
                             (assoc query-parameters :page ppage))]
         [:> react-bootstrap/ButtonGroup
          [:> react-bootstrap/Button
           {:variant "primary"
            :disabled (< ppage 1)
            :href ppagepath}
           " previous "]
          [:> react-bootstrap/Button
           {:variant "primary"
            :href (path hk route-params
                        (assoc query-parameters :page (inc current-page)))}
           " next "]])])))
