(ns leihs.admin.html
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    )
  (:require
    [leihs.core.anti-csrf.front :as anti-csrf]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]
    [leihs.core.user.front :as core-user]
    [leihs.core.user.shared :refer [short-id]]
    [leihs.core.env :refer [use-global-navbar?]]

    [leihs.admin.common.http-client.modals]
    [leihs.admin.state :as state]
    [leihs.admin.paths :refer [path]]

    [clojure.pprint :refer [pprint]]
    [accountant.core :as accountant]
    [reagent.dom :as rdom]
    ["@leihs/ui-components" :as UI]
    ))

(defn li-navitem [handler-key display-string]
  (let [active? (= (-> @routing/state* :handler-key) handler-key)]
    [:li.nav-item
     {:class (if active? "active" "")}
     [:a.nav-link {:href (path handler-key)} display-string]]))

(defn li-admin-navitem []
  (let [active? (boolean
                  (when-let [current-path (-> @routing/state* :path)]
                    (re-matches #"^/admin.*$" current-path)))]
    [:li.nav-item
     {:class (if active? "active" "")}
     [:a.nav-link {:href (path :admin)} "Admin"]]))

(defn sign-out-nav-component []
  [:form.form-inline.ml-2
   {:action (path :auth-sign-out {} {:target (-> @routing/state* :url)})
    :method :post}
   [:div.form-group
    [:input
     {:name :url
      :type :hidden
      :value (-> @routing/state* :url)}]]
   [anti-csrf/hidden-form-group-token-component]
   [:div.form-group
    [:label.sr-only
     {:for :sign-out}
     "Sign out"]
    [:button#sign-out.btn.btn-dark.form-group
     {:type :submit
      :style {:padding-top "0.2rem"
              :padding-bottom "0.2rem"}}
     [:span
      [:span " Sign out "]
      [:i.fas.fa-sign-out-alt]]]]])



(defn version-component []
  [:span.navbar-text "Version "
   (let [major (:version_major @state/leihs-admin-version*)
         minor (:version_minor @state/leihs-admin-version*)
         patch (:version_patch @state/leihs-admin-version*)
         pre (:version_pre @state/leihs-admin-version*)
         build (:version_build @state/leihs-admin-version*)]
     [:span
      [:span.major major]
      "." [:span.minor minor]
      "." [:span.patch patch]
      (when pre
        [:span "-"
         [:span.pre pre]])
      (when build
        [:span "+"
         [:span.build build]])])])

(defn current-page []
  [:div
   [leihs.admin.common.http-client.modals/modal-component]
   [:div
    (if-let [page (:page @routing/state*)]
      [page]
      [:div.page
       [:h1.text-danger 
        ;; NOTE: usage of this Bold component from leihs-ui seems pointless, but acts as a smoke test for the build system!
        [:> UI/Bold "Error 404 - There is no handler for the current path defined."]]])]
   [state/debug-component]
   [:nav.footer.navbar.navbar-expand-lg.navbar-dark.bg-secondary.col.mt-4
    [:div.col
     [:a.navbar-brand {:href (path :admin {})} "leihs-admin"]
     [version-component]]
    [:div.col
     [:a.navbar-text
      {:href (path :status)} "Admin-Status-Info"]]
    [state/debug-toggle-navbar-component]
    [:form.form-inline {:style {:margin-left "0.5em"
                                :margin-right "0.5em"}}]]])

(defn mount []
  (when-let [app (.getElementById js/document "app")]
    (rdom/render [current-page] app))
  (accountant/dispatch-current!))
