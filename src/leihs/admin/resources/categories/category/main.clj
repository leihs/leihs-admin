(ns leihs.admin.resources.categories.category.main
  (:refer-clojure :exclude [get])
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.shared :as shared]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [query update! delete!]
    :rename {query jdbc-query, update! jdbc-update! delete! jdbc-delete!}]))

;;; category ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn select-models-count [query]
  (sql/select query
              [(-> (sql/select :%count.*)
                   (sql/from :model_links)
                   (sql/where [:= :model_links.model_group_id :model_groups.id]))
               :models_count]))

(defn parents [tx category]
  (-> (apply sql/select shared/fields)
      (sql/select :model_group_links.label)
      (sql/from :model_group_links)
      (sql/join :model_groups
                [:= :model_group_links.parent_id :model_groups.id])
      (sql/where [:= :model_group_links.child_id (:id category)])
      sql-format
      (->> (jdbc-query tx))))

(defn query [id]
  (-> shared/base-query
      select-models-count
      (sql/where [:= :model_groups.id id])))

(defn get-one [tx id]
  (-> id query sql-format
      (->> (jdbc-query tx))
      first))

(defn get
  [{tx :tx {id :category-id} :route-params}]
  {:body (-> id query sql-format
             (->> (jdbc-query tx))
             first)})

;;; descendents ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn children [tx category]
  (-> (sql/select [:model_group_links.child_id :id]
                  :model_groups.name,
                  :model_group_links.label)
      select-models-count
      (sql/from :model_group_links)
      (sql/join :model_groups [:= :model_group_links.child_id :model_groups.id])
      (sql/where [:= :model_group_links.parent_id (:id category)])
      sql-format
      (->> (jdbc-query tx))))

(defn descendents [tx initial-category]
  (letfn [(descendents-h [category visited-ids]
            (if (contains? visited-ids (:id category))
              category
              (let [visited-ids* (conj visited-ids (:id category))
                    children-with-descendents (map #(descendents-h % visited-ids*)
                                                   (children tx category))]
                (assoc category :children children-with-descendents))))]
    (descendents-h initial-category #{})))

(comment
  (require '[clojure.inspector :as inspector])
  (require '[leihs.core.db :as db])
  (let [tx (db/get-ds)
        category (get-one tx #uuid "ec06d200-11a9-55f1-b9f5-cf4b36430c41")]
    ; category
    (parents tx category)
    ; (children tx category)
    ; (descendents tx category)
    ; (inspector/inspect-tree #_time (descendents tx category))
    ))

;;; delete category ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delete
  [{tx :tx {id :category-id} :route-params}]
  (assert id)
  (if (= id (:id (jdbc-delete! tx :model_groups
                               ["type = 'Category' AND id = ?" id]
                               {:return-keys true})))
    {:status 204}
    {:status 404 :body "Deleting category failed without error."}))

;;; update category ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn patch
  [{{id :category-id} :route-params tx :tx data :body :as request}]
  (when (get-one tx id)
    (jdbc-update! tx :model_groups
                  (select-keys data [:name])
                  ["type = 'Category' AND id = ?" id])
    {:status 204}))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn routes [request]
  (case (:request-method request)
    :get (get request)
    :patch (patch request)
    :delete (delete request)))

;#### debug ###################################################################

;(debug/wrap-with-log-debug #'data-url-img->buffered-image)
;(debug/wrap-with-log-debug #'buffered-image->data-url-img)
;(debug/wrap-with-log-debug #'resized-img)

;(debug/debug-ns *ns*)
