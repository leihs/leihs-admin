(ns leihs.admin.resources.mail-templates.mail-template.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [next.jdbc.sql :refer [query update!] :rename {query jdbc-query update! jdbc-update!}]))

;;; data keys ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def write-fields #{:subject :body})

;;; mail-template ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mail-template-query [mail-template-id]
  (-> (sql/select :*)
      (sql/from :mail_templates)
      (sql/where [:= :id mail-template-id])))

(defn mail-template
  ([tx mail-template-id]
   (mail-template tx mail-template-id nil))
  ([tx mail-template-id inventory-pool-id]
   (-> mail-template-id
       mail-template-query
       (cond-> inventory-pool-id
         (sql/where [:= :inventory_pool_id inventory-pool-id]))
       sql-format
       (->> (jdbc-query tx))
       first)))

(defn assert-global [template]
  (when-not (:is_template_template template)
    (throw (ex-info "This is not a global mail template (is_template_template)!"
                    {:status 403}))))

(defn get-mail-template
  [{tx :tx {mail-template-id :mail-template-id} :route-params}]
  (let [template (mail-template tx mail-template-id)]
    (assert-global template)
    {:body template}))

;;; update mail-template ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn patch-mail-template
  [{{mail-template-id :mail-template-id} :route-params tx :tx data :body :as request}]
  (let [template (mail-template tx mail-template-id)]
    (assert-global template))
  (when (-> (sql/select [true :exists])
            (sql/from :mail_templates)
            (sql/where [:= :id mail-template-id])
            sql-format
            (->> (jdbc-query tx))
            first :exists)
    (jdbc-update! tx :mail_templates
                  (select-keys data write-fields)
                  ["id = ?" mail-template-id])
    {:status 204}))

;;; routes and paths ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn routes [request]
  (case (:request-method request)
    :get (get-mail-template request)
    :patch (patch-mail-template request)))

;#### debug ###################################################################

;(debug/wrap-with-log-debug #'data-url-img->buffered-image)
;(debug/wrap-with-log-debug #'buffered-image->data-url-img)
;(debug/wrap-with-log-debug #'resized-img)

;(debug/debug-ns *ns*)
