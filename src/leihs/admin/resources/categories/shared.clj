(ns leihs.admin.resources.categories.shared
  (:require [honey.sql.helpers :as sql]))

(def fields [:model_groups.id
             :model_groups.name])

(def base-query
  (-> (apply sql/select fields)
      (sql/from :model_groups)
      (sql/where [:= :model_groups.type "Category"])
      (sql/order-by :model_groups.name)))
