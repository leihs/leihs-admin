(ns leihs.admin.utils.params
  (:require
   [clojure.string :as string]
   [ring.util.codec :as codec]
   [taoensso.timbre :refer [debug spy]]))

(defn cast-ids-to-uuids [x]
  (cond
    (vector? x) (vec (map cast-ids-to-uuids x))
    (map? x) (->> x
                  (map (fn [[k v]] [k (cast-ids-to-uuids v)]))
                  (into {}))
    (string? x) (try (java.util.UUID/fromString x)
                     ; Some IDs are not of type UUID (e.g. authentication_system).
                     (catch Exception _ x))
    :else x))

(defn url-decode [m]
  (->> m
       (map (fn [[k v]] [k (codec/form-decode v "UTF-8")]))
       (into {})))

(defn wrap-decode-and-cast-to-uuids [handler]
  (fn [request]
    (-> request
        (update :route-params (comp cast-ids-to-uuids url-decode))
        (update :query-params cast-ids-to-uuids)
        (update :form-params cast-ids-to-uuids)
        (update :params cast-ids-to-uuids)
        (cond-> (map? (:body request))
          (update :body cast-ids-to-uuids))
        handler)))
