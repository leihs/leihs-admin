(ns leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.shared
  (:require
   [clojure.set :refer [rename-keys]]
   [leihs.admin.common.form-components :as form-components]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.users.user.edit-core :as edit-core :refer [data*]]
   [leihs.core.routing.front :as routing]
   [react-bootstrap :as react-bootstrap :refer [Form]]
   [taoensso.timbre]))

(defn set-data-by-query-params [& _]
  (reset! data*
          (merge {:pool_protected true}
                 (-> @routing/state*
                     :query-params
                     (select-keys [:name :responsible_user_id :user-uid :pool_protected])
                     (rename-keys {:user-uid :responsible_user_id})))))

(defn responsible-user-choose-component []
  [:div.input-group-append
   [:a.btn.btn-info
    {:tab-index form-components/TAB-INDEX
     :href (path :users-choose {}
                 {:return-to (path (:handler-key @routing/state*)
                                   (:route-params @routing/state*)
                                   @data*)})}
    [:i.fas.fa-rotate-90.fa-hand-pointer.px-2]
    " Choose responsible user "]])

(defn delegation-form [& {:keys [id action] :or {id "delegation-form"}}]
  [:> Form {:id id
            :on-submit (fn [e] (.preventDefault e) (action))}
   [form-components/input-component data* [:name]
    :label "Name"]
   [form-components/input-component data* [:responsible_user_id]
    :label "Responsible user"
    :append responsible-user-choose-component]
   [:div
    [form-components/checkbox-component data* [:pool_protected]
     :label "Protected"
     :hint [:span
            "An " [:strong " unprotected "]
            " delegation can be " [:strong "added"] " to any other pool and then be used and "
            [:strong  " modified "] " from those pools in every way."
            " You can unprotect a delegation temporarily to share it with a limited number of pools."]]]])
