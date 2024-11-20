(ns leihs.admin.resources.categories.tree
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.category.descendents :refer [descendents]]
   [leihs.admin.resources.categories.shared :refer [base-query sql-add-metadata]]
   [next.jdbc.sql :as jdbc]))

(defn roots
  [tx & {:keys [with-metadata] :or {with-metadata false}}]
  (-> base-query
      (cond-> with-metadata (sql-add-metadata :label-col nil))
      (sql/where
       [:not
        [:exists
         (-> (sql/select 1)
             (sql/from :model_group_links)
             (sql/where [:= :model_group_links.child_id :model_groups.id]))]])
      sql-format
      ; (sql-format :inline true)
      (->> (jdbc/query tx))))

(defn tree [tx & {:keys [with-metadata] :or {with-metadata false}}]
  (map #(descendents tx % :with-metadata with-metadata)
       (roots tx :with-metadata with-metadata)))

(defn tree-path [node]
  (letfn [(tree-path-h [node result]
            (if (empty? (:children node))
              result
              (tree-path-h (first (:children node))
                           (conj result (dissoc node :children)))))]
    (tree-path-h node [])))

