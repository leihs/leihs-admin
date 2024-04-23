(ns leihs.admin.common.components.navigation.back
  (:require
   [cljs.core.async :as async :refer [<! go]]
   [clojure.string :as string]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.core.routing.front :as routing]
   [leihs.core.user.front :as current-user]
   [react-bootstrap :as react-bootstrap :refer [Breadcrumb BreadcrumbItem]]
   [reagent.core :as reagent]))

(defonce resolved* (reagent/atom []))

(defn find-index-of-name [data name]
  (->> data
       (map-indexed #(when (= (:name %2) name) %1))
       (keep-indexed #(when %2 %1))
       first))

(defn capitalize-and-replace [symbol]
  (as-> symbol s
    (name s)
    (if (clojure.string/includes? s "-")
      (as-> s st
        (string/split st #"-")
        (map string/capitalize st)
        (string/join " " st))
      (string/capitalize s))))

(defn update-resolved [data]
  (let [existing-index (find-index-of-name @resolved* (:name data))]
    (if existing-index
      (do
        (reset! resolved* (conj (subvec @resolved* 0 existing-index) data))
        (js/console.debug "resolved assoced" @resolved* data))
      (do
        (swap! resolved* conj data)
        (js/console.debug "resolved conjoined" @resolved*)))))

(def skip-handler [:users-choose
                   :user-choose]);])

(def reset-handler [:inventory-pools
                    :groups
                    :users
                    :inventory-fields
                    :buildings
                    :rooms
                    :suppliers
                    :mail-templates
                    :authentication-systems])

(defn resolve-name [next]
  (when (:resolver next)
    (go
      (let [resolver (:resolver next)
            res  (->
                  {:chan (async/chan)
                   :url (path resolver
                              (-> @routing/state* :route-params))}
                  http-client/request :chan <!
                  http-client/filter-success! :body)
            handler (:handler next)
            url (:url next)]
        (if (or (= resolver :inventory-pool-user)
                (= resolver :user))
          (update-resolved {:name (str (:firstname res) " " (:lastname res))
                            :url url
                            :handler handler})

          (update-resolved {:name (:name res)
                            :url url
                            :handler handler}))))))

(defn update-breadcrumbs []
  (when (and @current-user/state*
             (not (= (:handler-key @routing/state*) :admin))
             (not (some #(= (:handler-key @routing/state*) %) skip-handler)))
    (let [new-entry {:url (:route @routing/state*)
                     :handler (:handler-key @routing/state*)}
          handler (:handler-key @routing/state*)
          reset? (some #(= (:handler-key @routing/state*) %) reset-handler)]
      (js/console.debug "prior to cond" handler)
      (let [next-entry
            (cond
              (and (= handler :inventory-pool-delegations)
                   (not reset?))
              (conj new-entry {:resolver :inventory-pool})

              (and (re-matches #":inventory-pool-delegation.*" (str handler))
                   (not reset?))
              (conj new-entry {:resolver :inventory-pool-delegation})

              (and (or (= handler :inventory-pool-entitlement-group)
                       (re-matches #":inventory-pool-entitlement-group-.*" (str handler)))
                   (not reset?))
              (conj new-entry {:resolver :inventory-pool-entitlement-group})

              (and (re-matches #":inventory-pool-user" (str handler))
                   (not reset?))
              (conj new-entry {:resolver :inventory-pool-user})

              (and (re-matches #".*pool.*" (str handler))
                   (not reset?))
              (conj new-entry {:resolver :inventory-pool})

              (and (re-matches #":user.*" (str handler))
                   (not reset?))
              (conj new-entry {:resolver :user})

              (and (re-matches #":group.*" (str handler))
                   (not reset?))
              (conj new-entry {:resolver :group})

              :else (do
                      (js/console.debug "else branch")
                      (if reset?
                        (conj new-entry {:resolver nil
                                         :name (capitalize-and-replace handler)})
                        (conj new-entry {:resolver handler}))))]

        (if reset?
          (reset! resolved* [next-entry])
          (resolve-name next-entry))))
    [:<>]))

(defn button []
  (when (and (not (nil? (seq @resolved*)))
             (> (count @resolved*) 1))
    [:> Breadcrumb
     {:style {:max-width "fit-content"}}
     (doall (map-indexed (fn [index breadcrumb]
                           [:> BreadcrumbItem
                            {:style {:max-width (str (if (< (count @resolved*) 6) 200 50) "px")
                                     :text-overflow "ellipsis"
                                     :white-space "nowrap"
                                     :overflow "hidden"}
                             :key index
                             :href (str (:url breadcrumb))
                             :active (when (= (inc index) (count @resolved*)) true)}
                            (if-let [icon (:icon breadcrumb)]
                              [icon]
                              [:span {:on-click #(swap! resolved* (fn [data] (subvec data 0 (inc index))))}
                               (str (:name breadcrumb))])])

                         @resolved*))]))
