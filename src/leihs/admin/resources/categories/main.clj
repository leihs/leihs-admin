(ns leihs.admin.resources.categories.main
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.category.main :as category]
   [leihs.admin.resources.categories.shared :as shared]
   [leihs.admin.resources.categories.filter :as filter]
   [leihs.core.core :refer [presence]]
   [leihs.core.db :as db]
   [next.jdbc.sql :as jdbc]))

(defn roots [tx]
  (-> shared/base-query
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

(defn term-filter [tree request]
  (if-let [term (-> request :query-params-raw :term presence)]
    (filter/deep-filter #(re-matches (re-pattern (str "(?i).*" term ".*"))
                                     (:name %))
                        tree)
    tree))

(defn index [{tx :tx :as request}]
  {:body {:name "categories"
          :children (-> (tree tx)
                        (term-filter request))}})

(comment
  (require '[clojure.inspector :as inspector])
  (require '[leihs.core.db :as db])
  (let [tx (db/get-ds)]
    ; (roots tx)
    (tree tx)
    #_(deep-filter #(re-matches #"(?i).*audio.*" (:name %))
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
