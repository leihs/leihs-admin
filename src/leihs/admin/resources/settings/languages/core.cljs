(ns leihs.admin.resources.settings.languages.core
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.form-components :as form-components]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Form]]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defn fetch []
  (let [ch (async/chan)]
    (requests/send-off {:url (-> @routing/state* :url) :method :get} {} :chan ch)
    (go (let [resp (<! ch)]
          (when (:success resp)
            (reset! data* (:body resp)))))))

(defn form [action data*]
  [:> Form
   {:id "languages-form"
    :on-submit (fn [e]
                 (.preventDefault e)
                 (action))}
   [table/container
    {:header [:tr
              [:th "Locale"]
              [:th "Name"]
              [:th "Active"]
              [:th "Default"]]
     :body
     (doall
      (for [[locale lang] @data*]
        ^{:key locale}
        [:tr
         [:td (:locale lang)]
         [:td [:span (:name lang)]]
         [:td.active
          [form-components/checkbox-component
           data* [locale :active]
           :key (str locale "-" :active)
           :disabled (get-in @data* [locale :default])]]
         [:td.default
          [form-components/checkbox-component data*
           [locale :default]
           :key (str locale "-" :default)
           :disabled (or (-> @data* (get-in [locale :active]) not)
                         (get-in @data* [locale :default]))
           :pre-change (fn [v]
                         (doseq [locale (keys @data*)]
                           (swap! data* assoc-in [locale :default] false))
                         v)]]]))}]])
