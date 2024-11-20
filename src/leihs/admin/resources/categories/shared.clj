(ns leihs.admin.resources.categories.shared
  (:require [honey.sql :refer [format] :rename {format sql-format}]
            [honey.sql.helpers :as sql]
            [leihs.admin.utils.images :as images]
            [leihs.core.db :as db]
            [next.jdbc.sql :as jdbc]))

; NOTE: `category_id` alias due to requirements of FE plugin irt uniqueness of `id`s.
; As a category may appear in form of multiple nodes (under different label) in the
; category tree, this uniqueness is not guaranteed. Therefore, we use `category_id`.
(def fields [[:model_groups.id :category_id]
             :model_groups.name])

(defn sql-add-metadata [query & {:keys [label-col]}]
  (-> query
      (sql/select
       [[:json_build_object
         "id" :model_groups.id
         "name" :model_groups.name
         "label" label-col
         "models_count" (-> (sql/select :%count.*)
                            (sql/from :model_links)
                            (sql/where [:=
                                        :model_links.model_group_id
                                        :model_groups.id]))
         "thumbnail_url" [:|| images/IMG-DATA-URL-PREFIX "," :images.content]]
        :metadata])
      (sql/left-join :images
                     [:and
                      [:= :images.target_id :model_groups.id]
                      [:= :images.thumbnail true]])))

(def base-query
  (-> (apply sql/select fields)
      (sql/from :model_groups)
      (sql/where [:= :model_groups.type "Category"])
      (sql/order-by :model_groups.name)))

(comment (-> base-query
             (sql/limit 1)
             (sql-format :inline true)
             (->> (jdbc/query (db/get-ds)))))
