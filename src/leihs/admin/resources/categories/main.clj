(ns leihs.admin.resources.categories.main
  (:require
   [clojure.set]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.categories.category.main :as category]
   [leihs.admin.resources.categories.filter :as filter]
   [leihs.admin.resources.categories.tree :refer [tree]]
   [leihs.core.core :refer [presence]]
   [leihs.core.db :as db]
   [next.jdbc.sql :as jdbc]))

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
