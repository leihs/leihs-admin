(ns leihs.admin.utils.uuid
  (:require [clojure.string :as string]))

(defn cast-id-params-to-uuid [m]
  (->> m
       (map (fn [[k v]]
              [k (if (-> k name (string/ends-with? "-id"))
                   (java.util.UUID/fromString v)
                   v)]))
       (into {})))

(defn wrap-cast-id-params-to-uuid [handler]
  (fn [request]
    (-> request
        (update :route-params cast-id-params-to-uuid)
        (update :query-params cast-id-params-to-uuid)
        (update :form-params cast-id-params-to-uuid)
        (update :params cast-id-params-to-uuid)
        handler)))
