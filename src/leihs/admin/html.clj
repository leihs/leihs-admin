(ns leihs.admin.html
  (:require
    [clj-http.client :as client]
    [hiccup.page :refer [html5 include-js]]
    [leihs.admin.paths :refer [path]]
    [leihs.admin.state :refer [state*]]
    [leihs.admin.utils.release-info :as release-info]
    [leihs.core.http-cache-buster2 :as cache-buster]
    [leihs.core.json :refer [to-json]]
    [leihs.core.url.core :as url]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I>]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]
    [taoensso.timbre :refer [debug info warn error spy]]
    [leihs.core.remote-navbar.shared :refer [navbar-props]]
    ))

(defn include-site-css []
  (hiccup.page/include-css
    (cache-buster/cache-busted-path "/admin/ui/admin-ui.css")))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
   [:style "ol.breadcrumb.leihs-nav-right:empty {display: none}"]
   (include-site-css)])

(defn navbar-attribute [request]
  (let [navbar (navbar-props request {:admin false})]
    (-> navbar to-json url/encode)))

(defn body-attributes [request]
  {:data-user (some-> (:authenticated-entity request) to-json url/encode)
   :data-server-state (some-> @state*
                              (assoc :settings
                                     (-> request :settings
                                         (select-keys [:external_base_url])))
                              to-json url/encode)
   :data-navbar (navbar-attribute request)})

(defn not-found-handler [request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5
           (head)
           [:body
            (body-attributes request)
            [:div.container-fluid
             [:h1.text-danger "Error 404 - Not Found"]]])})

(defn html-handler [request]
  {:headers {"Content-Type" "text/html"}
   :body (html5
           (head)
           [:body (body-attributes request)
            [:div
             [:div#app
              [:div.container-fluid.alert.alert-warning
               [:h1 "Leihs Admin2"]
               [:p "This application requires Javascript."]]]]
            (hiccup.page/include-js
              (cache-buster/cache-busted-path "/admin/js/main.js"))])})


;#### debug ###################################################################
;(debug/debug-ns *ns*)
