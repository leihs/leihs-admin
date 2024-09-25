(ns leihs.admin.resources.categories.category.main
  (:refer-clojure :exclude [get])
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.main :as index]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :refer [query update! delete!]
    :rename {query jdbc-query, update! jdbc-update! delete! jdbc-delete!}]))

;;; building ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn query [id]
  (-> index/base-query
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

;;; delete group ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delete
  [{tx :tx {id :category-id} :route-params}]
  (assert id)
  (if (= id (:id (jdbc-delete! tx :model_groups
                               ["type = 'Category' AND id = ?" id]
                               {:return-keys true})))
    {:status 204}
    {:status 404 :body "Deleting category failed without error."}))

;;; update building ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
