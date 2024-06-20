(ns leihs.admin.utils.search-params
  (:require
   [accountant.core :as accountant]
   [clojure.string :as clj-str]
   [leihs.core.digest]
   [leihs.core.routing.front :as routing]))

(defn- set-params-in-url [url]
  (accountant/navigate! (clojure.string/replace
                         (.. url (toString))
                         (.. url -origin)
                         "")))

(defn- use-search-params []
  (let [url (new js/URL (:url @routing/state*))]
    [url (new js/URLSearchParams (.. url -search))]))

(defn- append [param-map]
  (let [[key value] (first param-map)
        [url params] (use-search-params)]
    (.. params (append (name key) value))
    (set! (.-search url) (.. params toString))
    url))

(defn- delete [name]
  (let [[url params] (use-search-params)]
    (.. params (delete name))
    (set! (.-search url) (.. params toString))
    url))

(defn append-to-url [param-map]
  (set-params-in-url
   (append param-map)))

(defn delete-from-url [name]
  (set-params-in-url
   (delete name)))

(defn delete-all-from-url []
  (let [[_ params] (use-search-params)
        keys (.. params (keys))]
    (doseq [key keys]
      (delete-from-url key))))

