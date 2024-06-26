(ns leihs.admin.resources.inventory-fields.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [bidi.bidi :refer [match-route]]
   [clojure.core.match :refer [match]]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.paths :refer [paths]]
   [leihs.admin.resources.inventory-fields.inventory-field.specs :as field-specs]
   [leihs.admin.resources.inventory-fields.shared :as shared]
   [leihs.admin.utils.seq :as seq]
   [leihs.core.core :refer [presence str]]
   [leihs.core.db :as db]
   [next.jdbc :refer [execute!] :rename {execute! jdbc-execute!}]
   [next.jdbc.sql :as jdbc]))

(def inventory-fields-base-query
  (-> (sql/select :fields.id :fields.dynamic :fields.active :fields.data)
      (sql/from :fields)
      (sql/order-by :fields.id)))

(defn set-per-page-and-offset
  ([query {per-page :per-page page :page}]
   (when (or (-> per-page presence not)
             (-> per-page integer? not)
             (> per-page 1000)
             (< per-page 1))
     (throw (ex-info "The query parameter per-page must be present and set to an integer between 1 and 1000."
                     {:status 422})))
   (when (or (-> page presence not)
             (-> page integer? not)
             (< page 0))
     (throw (ex-info "The query parameter page must be present and set to a positive integer."
                     {:status 422})))
   (set-per-page-and-offset query per-page page))
  ([query per-page page]
   (-> query
       (sql/limit per-page)
       (sql/offset (* per-page (- page 1))))))

(def searchable-expr [:concat :id " " :data])

(defn term-filter [query request]
  (if-let [term (-> request :query-params-raw :term presence)]
    (-> query
        (sql/where [:ilike searchable-expr (str "%" term "%")]))
    query))

(defn dynamic-filter [query request]
  (if-let [dynamic (-> request :query-params-raw :dynamic presence)]
    (case dynamic
      "yes" (sql/where query [:= :fields.dynamic true])
      "no" (sql/where query [:= :fields.dynamic false])
      :else query)
    query))

(defn target-type-filter [query request]
  (if-let [target-type (-> request :query-params-raw :target_type presence
                           #{"item" "license"})]
    (sql/where query [:= [:raw "data->>'target_type'"] target-type])
    query))

(defn active-filter [query request]
  (if-let [active (-> request :query-params-raw :active presence)]
    (case active
      "yes" (sql/where query [:= :fields.active true])
      "no" (sql/where query [:= :fields.active false])
      :else query)
    query))

(defn group-filter [query request]
  (if-let [group (-> request :query-params-raw :group presence)]
    (sql/where query [:= [:raw "data->>'group'"] (if (= group "none")
                                                   nil
                                                   group)])
    query))

(defn inventory-fields-query [request]
  (let [query-params (-> request :query-params
                         shared/normalized-query-parameters)]
    (-> inventory-fields-base-query
        (set-per-page-and-offset query-params)
        (term-filter request)
        (dynamic-filter request)
        (active-filter request)
        (group-filter request)
        (target-type-filter request))))

(defn inventory-fields [{tx :tx :as request}]
  (let [query (inventory-fields-query request)
        offset (:offset query)]
    {:body
     {:inventory-fields (-> query
                            sql-format
                            (->> (jdbc/query tx)
                                 (seq/with-index offset)
                                 seq/with-page-index))}}))

(defn get-inventory-fields-groups [tx]
  (-> inventory-fields-base-query
      sql-format
      (->> (jdbc/query tx)
           (map :data)
           (map :group)
           (filter identity)
           distinct)))

(comment
  (-> inventory-fields-base-query
      sql-format
      (->> (jdbc/query (db/get-ds))
           (map :fields/data)
           (map :target_type)
           (filter identity)
           distinct)))

(defn inventory-fields-groups [{tx :tx}]
  {:body
   {:inventory-fields-groups
    (get-inventory-fields-groups tx)}})

;;; create inventory-field ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-id [field]
  (string/join "_" (-> field :data :attribute)))

(defn disable-field-for-all-active-pools! [tx field-id]
  (jdbc-execute! tx
                 ["INSERT INTO disabled_fields (field_id, inventory_pool_id)
                  SELECT ?, id FROM inventory_pools WHERE is_active = TRUE",
                  field-id]))

(defn create-inventory-field [{tx :tx new-field :body :as request}]
  (let [inventory-field
        (jdbc/insert! tx :fields
                      (-> new-field
                          (->> (spec/assert ::field-specs/new-dynamic-field))
                          (merge field-specs/new-dynamic-field-constant-defaults)
                          (assoc :id (make-id new-field))))]
    (if-not inventory-field
      {:status 422, :body "No inventory-field has been created."}
      (do (disable-field-for-all-active-pools! tx (:id inventory-field))
          {:status 201, :body inventory-field}))))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn groups-route [request]
  (let [handler-key (->> request :uri (match-route paths) :handler)]
    (match [(:request-method request) handler-key]
      [:get :inventory-fields-groups] (inventory-fields-groups request))))

(defn routes [request]
  (let [handler-key (->> request :uri (match-route paths) :handler)]
    (match [(:request-method request) handler-key]
      [:get :inventory-fields] (inventory-fields request)
      [:post :inventory-fields] (create-inventory-field request))))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'groups-formated-query)
