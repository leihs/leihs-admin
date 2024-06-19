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

(defn- append [name value]
  (let [[url params] (use-search-params)
        search-params (.. params
                          (toString
                           (.. params (append name value))))]

    (set! (.-search url) search-params)
    url))

(defn- delete [name]
  (let [[url params] (use-search-params)
        search-params (.. params
                          (toString
                           (.. params (delete name))))]

    (set! (.-search url) search-params)
    url))

(defn append-to-url [name value]
  (set-params-in-url
   (append name value)))

(defn delete-from-url [name]
  (set-params-in-url
   (delete name)))

(defn delete-all-from-url []
  (let [[_ params] (use-search-params)
        keys (.. params (keys))]
    (doseq [key keys]
      (delete-from-url key))))

