(ns leihs.admin.resources.categories.category.main
  (:refer-clojure :exclude [get])
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.filter :refer [deep-filter]]
   [leihs.admin.resources.categories.shared :refer [base-query sql-add-metadata]]
   [leihs.admin.resources.categories.tree :refer [tree convert-tree-path roots]]
   [next.jdbc.sql :refer [delete! query update!]
    :rename {query jdbc-query, update! jdbc-update! delete! jdbc-delete!}]))

;;; category ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn query [id]
  (-> base-query
      (sql/where [:= :model_groups.id id])))

(defn merge-parents [tx category]
  (let [subtree (if (some #{(:category_id category)}
                          (map :category_id (roots tx)))
                  []
                  (deep-filter #(= (:category_id %) (:category_id category))
                               (tree tx)))]
    (assoc category :parents (map convert-tree-path subtree))))

(defn get-one [tx id]
  (-> id query
      (sql-add-metadata :label-col nil)
      sql-format
      (->> (jdbc-query tx))
      first
      (->> (merge-parents tx))))

(comment
  (do (require '[leihs.core.db :as db])
      (get-one (db/get-ds)
               #uuid "0f110e33-989c-5f84-a8dd-01e93dab4730")))

(defn get
  [{tx :tx {id :category-id} :route-params}]
  {:body (get-one tx id)})

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
