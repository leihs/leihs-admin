(ns leihs.admin.resources.settings.syssec.main
  (:require
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.resources.settings.shared.components :refer [row]]
   [leihs.admin.resources.settings.syssec.core :as core]
   [leihs.admin.resources.settings.syssec.edit :as edit]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn debug-component []
  (when @state/debug?*
    [:div.debug
     [:h3 "@data*"]
     [:pre (with-out-str (pprint @core/data*))]]))

(defn info-table []
  [table/container
   {:borders false
    :header [:tr [:th "Property"] [:th.w-75 "Value"]]
    :body
    [:<>
     [row "External Base URL" :external_base_url @core/data*]
     [row "Instance Element" :instance_element @core/data*]
     [row "Sessions Max Lifetime Secs" :sessions_max_lifetime_secs @core/data*]
     [row "Sessions Force Secure" :sessions_force_secure @core/data*]
     [row "Sessions Force Uniqueness" :sessions_force_uniqueness @core/data*]
     [row "Public Image Caching Enabled" :public_image_caching_enabled @core/data*]]}])

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount #(core/fetch)}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]
     [:article.settings-page.smtp

      [:header.my-5
       [:h1 [icons/shield-halved] " System and Security Settings"]]

      [:section.mb-5
       [info-table]
       [edit/button]
       [edit/dialog]]

      [debug-component]])])
