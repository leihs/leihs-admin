(ns leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.main
  (:require
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.core :as core]
   [leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.edit :as edit]
   [leihs.admin.resources.mail-templates.mail-template.edit :as global-edit]
   [leihs.admin.resources.mail-templates.mail-template.main :as global]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.routing.front :as routing]))

(defn page []
  [:<>
   [routing/hidden-state-component
    {:did-mount #(core/fetch)}]

   (if-not @core/data*
     [:div.my-5
      [wait-component]]

     [:article.mail-template
      [global/header @core/data*
       (path :inventory-pool-mail-template {:mail-template-id @core/id*,
                                            :inventory-pool-id @core/pool-id*})]

      [:section.mb-5
       [global/info-table @core/data*]
       [global-edit/button]
       [edit/dialog]]

      [core/debug-component]])])
