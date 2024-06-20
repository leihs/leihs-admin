(ns leihs.admin.resources.users.user.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.users.user.core :as user :refer [user-id*]]
   [leihs.admin.resources.users.user.edit-core :as edit-core :refer [data*]]
   [leihs.admin.resources.users.user.edit-image :as edit-image]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Form Modal]]
   [reagent.core :refer [reaction]]))

(defn patch []
  (go (when (some->
             {:chan (async/chan)
              :url (path :user {:user-id @user-id*})
              :method :patch
              :json-params  (-> @data*
                                (update-in [:extended_info]
                                           (fn [s] (.parse js/JSON s))))}
             http-client/request :chan <!
             http-client/filter-success!)
        (search-params/delete-all-from-url)
        (user/clean-and-fetch))))

(defn inner-form-component []
  [:div
   [edit-core/essentials-form-component]
   [:div.image.mt-5
    [:h3 "Image / Avatar"]
    [edit-image/image-component]]
   [edit-core/personal-and-contact-form-component]
   [edit-core/account-settings-form-component]])

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
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-all-from-url)}
    [:> Modal.Title "Edit User"]]
   [:> Modal.Body
    [:> Form {:id "add-user-form"
              :on-submit (fn [e]
                           (.preventDefault e)
                           (patch))}
     [inner-form-component]]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-all-from-url)}
     "Cancel"]
    [:> Button {:type "submit"
                :form "add-user-form"}
     "Save"]]])

(defn button []
  (when (auth/allowed? [user/modifieable?])
    [:<>
     [:> Button
      {:on-click #(search-params/append-to-url
                   {:action "edit"})}
      "Edit User"]
     [dialog]]))
