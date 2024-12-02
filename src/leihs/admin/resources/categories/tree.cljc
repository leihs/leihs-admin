(ns leihs.admin.resources.categories.tree
  #?(:clj
     (:require
      [honey.sql :refer [format] :rename {format sql-format}]
      [honey.sql.helpers :as sql]
      [leihs.admin.resources.categories.category.descendents :refer [descendents]]
      [leihs.admin.resources.categories.shared :refer [base-query sql-add-metadata]]
      [next.jdbc.sql :as jdbc])))

#?(:clj
   (defn roots
     [tx & {:keys [with-metadata] :or {with-metadata false}}]
     (-> base-query
         (cond-> with-metadata (sql-add-metadata :label nil))
         (sql/where
          [:not
           [:exists
            (-> (sql/select 1)
                (sql/from :model_group_links)
                (sql/where [:= :model_group_links.child_id :model_groups.id]))]])
         sql-format
         ; (sql-format :inline true)
         (->> (jdbc/query tx)))))

#?(:clj
   (defn tree [tx & {:keys [with-metadata] :or {with-metadata false}}]
     (map #(descendents tx % :with-metadata with-metadata)
          (roots tx :with-metadata with-metadata))))

(defn convert-tree-path
  "Converts a tree path represented as a map into a tree path
  represented as a vector (list of ancestors incl. self).
  Example:
  ;; (convert-tree-path {:id 1 :children [{:id 2 :children [{:id 3}]}]})
  ;; => [{:id 1} {:id 2} {:id 3}]"
  [node]
  (letfn [(tree-path-h [node result]
            (if (empty? (:children node))
              (conj result node)
              (tree-path-h (first (:children node))
                           (conj result (dissoc node :children)))))]
    (tree-path-h node [])))
