(ns leihs.admin.resources.settings.languages.main
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components :refer [toggle-component]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.resources.settings.languages.core :as languages-core]
   [leihs.admin.resources.settings.languages.edit :as edit]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button]]
   [reagent.core :as reagent]))

(defn debug-component []
  (when @state/debug?*
    [:div.debug
     [:h3 "@data*"]
     [:pre (with-out-str (pprint @languages-core/data*))]]))

(defn edit-button []
  (let [show (reagent/atom false)]
    (fn []
      [:<>
       [:> Button
        {:onClick #(reset! show true)}
        "Edit"]
       [edit/dialog {:show @show
                     :onHide #(reset! show false)}]])))
(defn info-table []

  (let [data @languages-core/data*]
    (fn []
      [:<>
       [table/container
        {:header [:tr
                  [:th "Locale"]
                  [:th "Name"]
                  [:th "Active"]
                  [:th "Default"]]
         :body
         (doall
          (for [[locale lang] data]
            ^{:key locale}
            [:tr
             [:td (:locale lang)]
             [:td [:span (:name lang)]]
             [:td (toggle-component (:active lang))]
             [:td (toggle-component (:default lang))]]))}]
       [edit-button]])))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-change languages-core/clean-and-fetch}]

   [:article.settings-page.smtp
    [:header.my-5
     [:h1 [icons/language] " Languages Settings"]]
    [:section
     (if-not @languages-core/data*
       [wait-component]
       [info-table])
     [debug-component]]]])
