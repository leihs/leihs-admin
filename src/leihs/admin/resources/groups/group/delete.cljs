(ns leihs.admin.resources.groups.group.delete
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.groups.group.core :as core]
   [leihs.admin.utils.search-params :as search-params]
   [leihs.core.auth.core :as auth]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]
   [reagent.core :refer [reaction]]))

;;; delete ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :group (-> @routing/state* :route-params))
                        :method :delete} {} :chan ch)
    (go (when (:success (<! ch))
          (search-params/delete-from-url "action")
          (accountant/navigate! (path :groups {}))))))

(def open*
  (reaction
   (->> (:query-params @routing/state*)
        :action
        (= "delete"))))

(defn dialog []
  [:> Modal {:size "sm"
             :centered true
             :scrollable true
             :show @open*}
   [:> Modal.Header {:closeButton true
                     :on-hide #(search-params/delete-from-url "action")}
    [:> Modal.Title "Delete Group"]]
   [:> Modal.Body
    "Are you sure you want to delete this group?"]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :on-click #(search-params/delete-from-url "action")}
     "Cancel"]
    [:> Button {:variant "danger"
                :onClick #(post)}
     "Delete"]]])

(defn button []
  (when (auth/allowed?
         [core/admin-and-group-not-system-admin-protected?
          auth/system-admin-scopes?
          core/some-lending-manager-and-group-unprotected?])
    [:<>
     [:> Button
      {:variant "danger"
       :className "ml-3"
       :on-click #(search-params/append-to-url {:action "delete"})}
      "Delete"]]))
