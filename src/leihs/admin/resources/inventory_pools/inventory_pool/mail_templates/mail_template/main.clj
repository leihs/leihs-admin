(ns leihs.admin.resources.inventory-pools.inventory-pool.mail-templates.mail-template.main
  (:require
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.resources.mail-templates.mail-template.main :as global]
   [next.jdbc.sql :refer [query update!] :rename {query jdbc-query update! jdbc-update!}]))

;;; mail-template ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-mail-template
  [{tx :tx {:keys [mail-template-id inventory-pool-id]} :route-params}]
  (let [template (global/mail-template tx mail-template-id inventory-pool-id)]
    {:body template}))

;;; update mail-template ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn patch-mail-template
  [{{:keys [mail-template-id inventory-pool-id]} :route-params
    tx :tx data :body :as request}]
  (when (-> (sql/select [true :exists])
            (sql/from :mail_templates)
            (sql/where [:= :inventory_pool_id inventory-pool-id])
            (sql/where [:= :id mail-template-id])
            sql-format
            (->> (jdbc-query tx))
            first :exists)
    (jdbc-update! tx :mail_templates
                  (select-keys data global/write-fields)
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
