(ns leihs.admin.common.components.table
  (:require
   [leihs.core.paths :refer [path]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button ButtonGroup ButtonToolbar Table]]))

(defn pagination []
  (let [hk (some-> @routing/state* :handler-key)
        route-params (or (some-> @routing/state* :route-params) {})
        query-parameters (some-> @routing/state* :query-params-raw)
        current-page (or (some-> query-parameters :page int) 1)]
    (if-not hk
      [:div "pagination not ready"]
      [:div
       (let [ppage (dec current-page)
             ppagepath (path hk route-params
                             (assoc query-parameters :page ppage))]
         [:> ButtonGroup
          [:> Button
           {:variant "primary"
            :disabled (< ppage 1)
            :href ppagepath}
           " previous "]
          [:> Button
           {:variant "primary"
            :href (path hk route-params
                        (assoc query-parameters :page (inc current-page)))}
           " next "]])])))

(defn toolbar [items]
  [:> ButtonToolbar {:className "my-3"}
   [pagination]
   items])

(defn container
  [& {:keys [className header body footer actions borders]
      :or {className nil
           header nil
           body nil
           footer nil
           actions nil
           borders true}}]
  [:section
   [:<> actions]
   (when borders
     [:hr])
   [:> Table {:striped true
              :borderless true
              :className className}
    [:thead
     header]
    [:tbody
     body]
    [:tfoot
     footer]]
   (when borders
     [:hr])
   [:<> actions]])

