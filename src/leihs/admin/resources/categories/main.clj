(ns leihs.admin.resources.categories.main
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.category.main :as category]
   [leihs.admin.resources.categories.shared :as shared]
   [leihs.admin.utils.pagination :as page]
   [leihs.admin.utils.query-params :as query-params]
   [leihs.admin.utils.seq :as seq]
   [leihs.core.core :refer [presence]]
   [leihs.core.db :as db]
   [next.jdbc.sql :as jdbc]))

(defn query [request]
  (let [query-params (-> request
                         :query-params
                         query-params/normalized-query-parameters)]
    (-> shared/base-query
        (page/set-per-page-and-offset query-params)
        #_(term-filter request))))

(defn roots [tx]
  (-> shared/base-query
      (sql/select [nil :label])
      category/select-models-count
      (sql/where
       [:not
        [:exists
         (-> (sql/select 1)
             (sql/from :model_group_links)
             (sql/where [:= :model_group_links.child_id :model_groups.id]))]])
      sql-format
      ; (sql-format :inline true)
      (->> (jdbc/query tx))))

(defn tree [tx]
  (map #(category/descendents tx %) (roots tx)))

(defn current-or-any-descendent? [pred category]
  (or (pred category)
      (some (partial current-or-any-descendent? pred)
            (:children category))))

(defn deep-filter [pred tree]
  (->> tree
       (filter (partial current-or-any-descendent? pred))
       (map #(update % :children (partial deep-filter pred)))))

(defn term-filter [tree request]
  (if-let [term (-> request :query-params-raw :term presence)]
    (deep-filter #(re-matches (re-pattern (str "(?i).*" term ".*"))
                              (:name %))
                 tree)
    tree))

(defn index [{tx :tx :as request}]
  {:body {:categories (-> (tree tx)
                          (term-filter request))}})

(comment
  (require '[clojure.inspector :as inspector])
  (require '[leihs.core.db :as db])
  (let [tx (db/get-ds)]
    (deep-filter #(re-matches #"(?i).*audio.*" (:name %))
                 (tree tx))))

;;; create category ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create [{tx :tx data :body :as request}]
  (if-let [category (jdbc/insert! tx
                                  :model_groups
                                  (-> data
                                      (select-keys [:name])
                                      (assoc :type "Category")))]
    {:status 201, :body category}
    {:status 422
     :body "No category has been created."}))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn routes [request]
  (case (:request-method request)
    :get (index request)
    :post (create request)))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'groups-formated-query)
