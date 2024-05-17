(ns leihs.admin.resources.inventory-pools.inventory-pool.workdays.edit
  (:require
   [clojure.string :refer [capitalize]]
   [leihs.admin.resources.inventory-pools.authorization :as pool-auth]
   [leihs.admin.resources.inventory-pools.inventory-pool.workdays.core :as core]
   [leihs.admin.utils.misc :refer [wait-component]]
   [leihs.core.auth.core :as auth]
   [react-bootstrap :as BS :refer [Button Modal]]
   [reagent.core :as reagent]))

(defn form []
  (if-not @core/data*
    [wait-component]
    [:div.workdays.mt-3
     (doall (for [day (keys core/DAYS)]
              [:div.mb-3 (capitalize (name day))
               #_[form-components/switch-component core/data* [:is_active]
                  :disabled (not @current-user/admin?*)
                  :label "Active"]]))]))

(defn dialog [& {:keys [show onHide] :or {show false}}]
  [:> Modal {:size "lg"
             :centered true
             :show show}
   [:> Modal.Header {:closeButton true
                     :onHide onHide}
    [:> Modal.Title "Edit Workdays"]]
   [:> Modal.Body [form]]
   [:> Modal.Footer
    [:> Button {:variant "secondary" :onClick onHide}
     "Cancel"]
    [:> Button {:onClick #(do (js/console.log "PATCH") (onHide))}
     "Save"]]])

(defn button []
  (when (auth/allowed? [pool-auth/pool-inventory-manager?
                        auth/admin-scopes?])
    (let [show (reagent/atom false)]
      (fn []
        [:<>
         [:> Button
          {:className ""
           :onClick #(reset! show true)}
          "Edit"]
         [dialog {:show @show
                  :onHide #(reset! show false)}]]))))
