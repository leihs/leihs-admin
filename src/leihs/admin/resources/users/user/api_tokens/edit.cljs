(ns leihs.admin.resources.users.user.api-tokens.edit
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.users.user.api-tokens.core :as core]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Button Modal]]))

(defn patch [callback]
  (let [ch (async/chan)]
    (requests/send-off {:url (path :user-api-token (-> @routing/state* :route-params))
                        :method :patch
                        :json-params @core/data*}
                       {}
                       :chan ch)
    (go (when (:success (<! ch))
          (callback)))))

(defn dialog [& {:keys [show on-hide on-save-callback]
                 :or {show false}}]
  [:> Modal {:size "xl"
             :centered true
             :scrollable true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide on-hide}
    [:> Modal.Title "Add API Token"]]
   [:> Modal.Body
    [core/form {:on-save #(patch on-save-callback)}]]
   [:> Modal.Footer
    [:> Button {:variant "secondary"
                :onClick on-hide}
     "Cancel"]
    [:> Button {:type "submit"
                :form "api-token-form"}
     "Save"]]])
