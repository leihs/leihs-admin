(ns leihs.admin.resources.categories.shared
  (:require [honey.sql.helpers :as sql]))

(def base-query
  (-> (sql/select :model_groups.id
                  :model_groups.name)
      (sql/from :model_groups)
      (sql/where [:= :model_groups.type "Category"])
      (sql/order-by :model_groups.name)))
