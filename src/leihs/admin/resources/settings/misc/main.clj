(ns leihs.admin.resources.settings.misc.main
  (:refer-clojure :exclude [str keyword])
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.string :as string]
   [compojure.core :as cpj]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.resources.audits.requests.shared :refer [default-query-params]]
   [leihs.admin.utils.jdbc :as utils-jdbc]
   [leihs.core.auth.core :as auth]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.routing.back :as routing :refer [set-per-page-and-offset wrap-mixin-default-query-params]]
   [leihs.core.sql :as sql]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]))

(defn get-misc-settings [{tx :tx}]
  {:body (-> (sql/select :*)
             (sql/from :settings)
             sql/format
             (->> (jdbc/query tx) first)
             (or (throw (ex-info "misc-settings not found" {:status 404})))
             (dissoc :id))})

(defn upsert [{tx :tx data :body :as request}]
  (utils-jdbc/insert-or-update! tx :settings ["id = 0"] data)
  (get-misc-settings request))

(def misc-settings-path (path :misc-settings {}))

(def routes
  (-> (cpj/routes
       (cpj/GET misc-settings-path [] #'get-misc-settings)
       (cpj/PATCH misc-settings-path [] #'upsert)
       (cpj/PUT misc-settings-path [] #'upsert))))

;#### debug ###################################################################

;(debug/debug-ns *ns*)
