(ns leihs.admin.resources.groups.group.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.group.core :as core :refer [clean-and-fetch
                                                             data* group-id*]]
   [leihs.admin.resources.groups.group.edit-core :as edit-core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Form Modal]]
   [reagent.core :refer [reaction]]))

(defn patch []
  (go (when (some->
             {:chan (async/chan)
              :url (path :group {:group-id @group-id*})
              :method :patch
              :json-params @data*}
             http-client/request :chan <!
             http-client/filter-success!)
        (search-params/delete-from-url "action")
        (clean-and-fetch))))

(def open*
  (reaction
   (->> (:query-params @routing/state*)
        :action
        (= "edit"))))

(defn dialog []
  [:> Modal {:size "xl"
             :centered true
             :scrollable true
             :show @open*}
   [:> Modal.Header {:close-button true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Edit Group"]]
   [:> Modal.Body
    [:> Form {:id "add-user-form"
              :on-submit (fn [e]
                           (.preventDefault e)
                           (patch))}
     [edit-core/inner-form-component]]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-user-form"}
     "Save"]]])

(defn button []
  (when (auth/allowed?
         [core/admin-and-group-not-system-admin-protected?
          auth/system-admin-scopes?
          core/some-lending-manager-and-group-unprotected?])
    [:<>
     [:> Button
      {:onClick #(search-params/append-to-url {:action "edit"})}
      "Edit"]
     [dialog]]))
