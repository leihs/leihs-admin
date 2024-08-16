(ns leihs.admin.resources.mail-templates.mail-template.main
  (:require
   [leihs.admin.common.components.navigation.breadcrumbs :as breadcrumbs]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.mail-templates.mail-template.core :as core]
   [leihs.admin.resources.mail-templates.mail-template.edit :as edit]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn info-table [data]
  [table/container
   {:className "mail-template"
    :borders false
    :header [:tr [:th "Property"] [:th.w-75 "Value"]]
    :body
    [:<>
     [:tr.name
      [:td "Name" [:small " (name)"]]
      [:td.name  (:name data)]]
     [:tr.type
      [:td "Type" [:small " (type)"]]
      [:td.type (:type data)]]
     [:tr.language
      [:td "Language Locale" [:small " (language_locale)"]]
      [:td.language (:language_locale data)]]
     [:tr.format
      [:td "Format" [:small " (format)"]]
      [:td.format (:format data)]]
     [:tr.body {:style {:white-space "break-spaces"}}
      [:td "Body" [:small " (body)"]]
      [:td.body (:body data)]]
     [:tr.variables {:style {:white-space "break-spaces"}}
      [:td "Variables" [:small " (variables)"]]
      [:td.variables
       (core/template-variables)]]]}])

(defn header [data path]
  (let [name (:name data)]
    (fn []
      [:header.my-5
       [breadcrumbs/main {:to path}]
       [:h1.mt-3 name]])))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount #(core/fetch)}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]

     [:article.mail-template
      [header @core/data* (path :mail-templates)]

      [:section.mb-5
       [info-table @core/data*]
       [edit/button]
       [edit/dialog]]

      [core/debug-component]])])
