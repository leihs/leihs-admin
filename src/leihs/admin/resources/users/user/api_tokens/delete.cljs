(ns leihs.admin.resources.users.user.api-tokens.delete
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async :refer [go <!]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]))

(defn delete []
  (let [ch (async/chan)]
    (requests/send-off {:url (path :user-api-token (-> @routing/state* :route-params))
                        :method :delete}
                       {}
                       :chan ch)
    (go (when (:success (<! ch))
          (accountant/navigate!
           (path :user (-> @routing/state* :route-params)))))))

(defn dialog [& {:keys [show on-hide]
                 :or {show false}}]
  [:> Modal {:size "sm"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide on-hide}
    [:> Modal.Title "Delete API Token"]]
   [:> Modal.Body
    "Are you sure you want to delete this api token"]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick on-hide}
     "Cancel"]
    [:> Button {:variant "danger"
                :onClick #(do (on-hide)
                              (delete))}
     "Delete"]]])
