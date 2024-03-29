(ns leihs.admin.resources.settings.syssec.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [timeout]]
   [cljs.pprint :refer [pprint]]
   [leihs.admin.common.components :as components]

   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as admin.common.icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.settings.icons :as icons]
   [leihs.admin.resources.settings.syssec.breadcrumbs :as breadcrumbs]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :refer [wait-component]]

   [leihs.core.breadcrumbs :as core-breadcrumbs]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent]))

(defonce data* (reagent/atom nil))

(defonce edit?* (reagent/atom false))

(defn fetch [& _]
  (go (reset! data*
              (some-> {:chan (async/chan)}
                      http-client/request
                      :chan <! http-client/filter-success! :body))))

(defn put [& _]
  (go (when-let [data (some->
                       {:chan (async/chan)
                        :json-params @data*
                        :method :put}
                       http-client/request
                       :chan <!
                       http-client/filter-success :body)]
        (reset! data* data)
        (reset! edit?* false))))

(defn form-component []
  [:form.form
   {:on-submit (fn [e]
                 (.preventDefault e)
                 (put))}
   [:div
    [form-components/input-component data* [:external_base_url]
     :disabled (not @edit?*) :label "Base URL"]]

   [:div
    [form-components/input-component data* [:instance_element]
     :disabled (not @edit?*) :rows 3 :element :textarea
     :hint [:span "Some custom html/text. "]]]

   [:div.row
    [:div.col-sm
     [form-components/input-component data* [:sessions_max_lifetime_secs]
      :type :number :disabled (not @edit?*)]]
    [:div.col-sm
     [form-components/checkbox-component data* [:sessions_force_secure]
      :disabled (not @edit?*)]]
    [:div.col-sm
     [form-components/checkbox-component data* [:sessions_force_uniqueness]
      :disabled (not @edit?*)]]]

   [:div.row
    [:div.col-sm
     [form-components/checkbox-component data* [:public_image_caching_enabled]
      :hint [:div
             [:p (str "Sets http-headers such that images are treated as public available resources. "
                      "This enables caching of the images at various stages. "
                      "It does not expose the images to crawlers since the listing of the images is not public! ")]
             [:p (str "We recommend leave this setting enabled as it generally improves user experience and lifts load from the application server. ")]]
      :disabled (not @edit?*)]]]

   (when @edit?*
     [form-components/save-submit-component])])

(defn main-component []
  (if-not @data*
    [wait-component]
    [form-component]))

(defn debug-component []
  (when @state/debug?*
    [:div.debug
     [:h3 "@data*"]
     [:pre (with-out-str (pprint @data*))]]))

(defn page []
  [:div.settings-page
   [routing/hidden-state-component
    {:did-mount (fn [& _]
                  (reset! edit?* false)
                  (fetch))}]
   [breadcrumbs/nav-component
    @breadcrumbs/left*
    [[:li.breadcrumb-item
      [:button.btn
       {:class (if @edit?*
                 core-breadcrumbs/disabled-button-classes
                 core-breadcrumbs/enabled-button-classes)
        :on-click #(reset! edit?* true)}
       [admin.common.icons/edit] " Edit"]]]]
   [:h1 icons/syssec " System and Security Settings"]
   [main-component]
   [debug-component]])
