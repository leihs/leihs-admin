(ns leihs.admin.resources.categories.category.models.main
  (:require
   [leihs.admin.common.components.table :as table]
   [leihs.admin.resources.categories.category.core :as core]))

(defn models-in-category-table []
  [:<>
   [table/container
    {:borders false
     :header [:tr [:th "Model name"] [:th.w-75 "Assigned in inventory pool"]]
     :body
     [:<>
      (for [model @core/data-models*]
        [:tr.model-relation {:key (:id model)}
         [:td.w-50 (:name model)]
         [:td [:ul.p-0.pl-3.m-0
               (for [used-in-pools (:used-in-pools model)]
                 [:li {:key (:id used-in-pools)}
                  [:p (:name used-in-pools)]])]]])]}]])
