(ns leihs.admin.resources.audits.changes.main
  (:require
   ["date-fns" :refer [subDays format parseISO isAfter isValid]]
   [accountant.core :as accountant]
   [cljs.pprint :refer [pprint]]
   [clojure.string :refer [join]]
   [leihs.admin.common.components :as components]
   [leihs.admin.common.components.filter :as filter]
   [leihs.admin.common.components.table :as table]
   [leihs.admin.common.http-client.core :as http-client]
   [leihs.admin.common.icons :as icons]
   [leihs.admin.paths :as paths :refer [path]]
   [leihs.admin.resources.audits.changes.shared :refer [default-query-params]]
   [leihs.admin.state :as state]
   [leihs.admin.utils.misc :as front-shared :refer [wait-component]]
   [leihs.core.core :refer [presence]]
   [leihs.core.routing.front :as routing]
   [reagent.core :as reagent :refer [reaction]]))

;;; data ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce data* (reagent/atom {}))

(defn page-path-for-query-params [query-params]
  (path (:handler-key @routing/state*)
        (:route-params @routing/state*)
        (merge default-query-params (:query-params-raw @routing/state*) query-params)))

(defn fetch-changes [& _]
  (http-client/route-cached-fetch data* :reload true))

;;; meta ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def tables*
  (reaction
   (let [query-param-table (some-> @routing/state* :query-params-raw :table presence)
         meta-tables (some-> @data*
                             (get-in [(:route @routing/state*) :meta :tables])
                             seq)
         tables (->> (concat [] meta-tables [query-param-table])
                     (map presence) (filter identity) distinct sort
                     (map (fn [t] [t t])))]
     (concat [["(any)" ""]]
             tables))))

;;; helper ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn normalize-date-str [s]
  (cond
    ;; Compact digits: YYYYMMDD -> YYYY-MM-DD
    (re-matches #"^\d{8}$" s)
    (let [y (subs s 0 4)
          m (subs s 4 6)
          d (subs s 6 8)]
      (str y "-" m "-" d))

    ;; Compact digits: YYYYMM -> YYYY-MM
    (re-matches #"^\d{6}$" s)
    (let [y (subs s 0 4)
          m (subs s 4 6)]
      (str y "-" m))

    :else
    (let [[_ y m d] (re-matches #"^(\d{1,6})(?:-(\d{1,2}))?(?:-(\d{1,2}))?$" s)
          y4 (when y (subs y 0 (min 4 (count y))))
          m2 (when m (if (= 1 (count m)) (str "0" m) m))
          d2 (when d (if (= 1 (count d)) (str "0" d) d))]
      (cond
        (and y4 m2 d2) (str y4 "-" m2 "-" d2)
        (and y4 m2)    (str y4 "-" m2)
        :else          s))))

(defn year-gte-1900? [s]
  (when (= 10 (count s))
    (let [y-str (subs s 0 4)
          y (js/parseInt y-str 10)]
      (>= y 1900))))

(defn valid-date-str? [s]
  (when (= 10 (count s))
    (let [d (parseISO s)]
      (and (isValid d) (year-gte-1900? s)))))

(defn invalid-date-str? [s]
  (cond
    (< (count s) 10) false
    :else (not (valid-date-str? s))))

;;; filters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn table-filter-component []
  (let [disabled* (reaction (-> @tables* empty?))]
    (fn []
      [:div.form-group.m-2
       [:label {:for :table}
        [:span "Table name " [:small.text_monspache "(table)"]]]
       [:select#table.custom-select
        {:value (:table (merge default-query-params
                               (:query-params-raw @routing/state*)))
         :disabled @disabled*
         :on-change (fn [e]
                      (let [val (or (-> e .-target .-value presence) "")]
                        (accountant/navigate! (page-path-for-query-params
                                               {:page 1
                                                :table val}))))}
        (for [[n v] @tables*]
          ^{:key n} [:option {:value v} n])]])))

(defn tg-op-filter-component []
  [:div.form-group.m-2
   [:label {:for :tg-op}
    [:span "Operation " [:small.text_monspache "(tg-op)"]]]
   [:select#tg-op.custom-select
    {:value (:tg-op (merge default-query-params
                           (:query-params-raw @routing/state*)))
     :on-change (fn [e]
                  (let [val (or (-> e .-target .-value presence) "")]
                    (accountant/navigate! (page-path-for-query-params
                                           {:page 1
                                            :tg-op val}))))}
    (for [[n v] (->> ["" "DELETE" "INSERT" "UPDATE"]
                     (map (fn [op] [op op])))]
      ^{:key n} [:option {:value v} n])]])

(defn date-filter
  [key-kw id-str label-text input-atom update-range! update-range-debounced! invalid?]
  [:div.form-group.m-2
   [:label {:for id-str}
    [:span (str label-text " ") [:small.text-monospace (str "(" id-str ")")]]]
   [(keyword (str "input#" id-str ".form-control"))
    {:type "date"
     :min "1900-01-01"
     :class (str "form-control" (when @invalid? " is-invalid"))
     :value @input-atom
     :on-change #(update-range-debounced! key-kw (.. % -target -value))
     :on-input  #(update-range-debounced! key-kw (.. % -target -value))
     :on-key-up (fn [e]
                  (let [val (.. e -target -value)
                        key (.-key e)]
                    (if (= key "Enter")
                      (update-range! key-kw val)
                      (update-range-debounced! key-kw val))))
     :on-blur #(update-range! key-kw (.. % -target -value))}]
   (when @invalid?
     [:div.invalid-feedback "Please enter a valid date (>= 1900-01-01)."])])

(defn time-range-filter-component []
  (let [query-params (merge default-query-params
                            (:query-params-raw @routing/state*))
        fmt-date #(format % "yyyy-MM-dd")
        today (js/Date.)
        one-week-ago (subDays today 7)
        start-date (reagent/atom (or (not-empty (:start-date query-params))
                                     (fmt-date one-week-ago)))
        end-date   (reagent/atom (or (not-empty (:end-date query-params))
                                     (fmt-date today)))
        start-input (reagent/atom @start-date)
        end-input   (reagent/atom @end-date)
        start-invalid? (reagent/atom false)
        end-invalid?   (reagent/atom false)
        timeout-id (atom nil)
        ensure-valid-range! (fn []
                              (let [sd-str @start-date
                                    ed-str @end-date
                                    sd (when (= 10 (count sd-str)) (parseISO sd-str))
                                    ed (when (= 10 (count ed-str)) (parseISO ed-str))]
                                (when (and sd ed (isValid sd) (isValid ed)
                                           (year-gte-1900? sd-str)
                                           (year-gte-1900? ed-str)
                                           (isAfter sd ed))
                                  (reset! start-date (fmt-date (subDays ed 7)))
                                  (reset! start-input @start-date)
                                  (js/console.warn "⚠ adjusted start-date ->" @start-date))))
        update-range! (fn [k v]
                        (let [v* (normalize-date-str v)]
                          (when @timeout-id
                            (js/clearTimeout @timeout-id)
                            (reset! timeout-id nil))
                          (case k
                            :start (do (reset! start-date v*) (reset! start-input v*)
                                       (reset! start-invalid? (invalid-date-str? v*)))
                            :end   (do (reset! end-date v*) (reset! end-input v*)
                                       (reset! end-invalid? (invalid-date-str? v*))))
                          (ensure-valid-range!)
                          (let [sd-str @start-date
                                ed-str @end-date
                                curr (:query-params-raw @routing/state*)]
                            (when (and (valid-date-str? sd-str)
                                       (valid-date-str? ed-str)
                                       (or (not= (:start-date curr) sd-str)
                                           (not= (:end-date curr) ed-str)))
                              (accountant/navigate!
                               (page-path-for-query-params
                                {:page 1
                                 :start-date sd-str
                                 :end-date ed-str}))))))
        update-invalid-flags! (fn []
                                (reset! start-invalid? (invalid-date-str? @start-input))
                                (reset! end-invalid?   (invalid-date-str? @end-input)))
        debounced-update-range! (fn [k v]
                                  (case k
                                    :start (reset! start-input v)
                                    :end   (reset! end-input v))
                                  (update-invalid-flags!)
                                  (when @timeout-id
                                    (js/clearTimeout @timeout-id))
                                  (let [nv (normalize-date-str v)]
                                    (reset! timeout-id
                                            (js/setTimeout
                                             (fn []
                                               (when (valid-date-str? nv)
                                                 (update-range! k nv)))
                                             filter/DURATION_DEBOUNCE))))]
    (fn []
      [:div.d-flex.flex-wrap
       [date-filter :start "start-date" "Start date" start-input update-range! debounced-update-range! start-invalid?]
       [date-filter :end   "end-date"   "End date"   end-input   update-range! debounced-update-range! end-invalid?]])))

(defn filter-component []
  [filter/container
   [:<>
    [filter/delayed-query-params-input-component
     :label "Search in changed data"
     :query-params-key :term
     :input-options {:placeholder "fuzzy term"}]
    [filter/delayed-query-params-input-component
     :label "TXID"
     :query-params-key :txid
     :input-options {:placeholder "transaction id"}]
    [filter/delayed-query-params-input-component
     :label "Primary key"
     :query-params-key :pkey]
    [table-filter-component]
    [tg-op-filter-component]
    [time-range-filter-component]
    [filter/form-per-page]
    [filter/reset :default-query-params default-query-params]]])

;;; table ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn thead-component [hds]
  [:tr
   [:th {:key :timestamp} "Timestamp"]
   [:th {:key :txid} "TX ID"]
   [:th {:key :pkey} "Pkey"]
   [:th {:key :table} "Table"]
   [:th {:key :tg-op} "Operation"]
   [:th {:key :changed-attributes} "Changed attributes"]
   [:th {:key :request}]
   [:th {:key :change}]
   (for [[idx hd] (map-indexed vector hds)]
     ^{:key idx} [hd])])

(defn row-component [change tds]
  [:tr.user
   {:key (:id change)}
   [:td.text-monospace.timestamp (:created_at change)]
   [:td [components/truncated-id-component (:txid change) :key :txid]]
   [:td [components/truncated-id-component (:pkey change) :key :pkey]]
   [:td.table-name (:table_name change)]
   [:td.tg-op (:tg_op change)]
   [:td.changed-attributes
    {:style {:max-width "20em"}}
    (->> change :changed_attributes (map str) (join ", "))]
   [:td.request
    (when (:has_request change)
      [:a {:href (path :audited-request {:request-id (:request-id change)})}
       [:span [icons/view] " Request "]])]
   [:td.change
    [:a {:href (path :audited-change {:audited-change-id (:id change)})}
     [:span [icons/view] " Change "]]]
   (for [[idx col] (map-indexed vector tds)]
     ^{:key idx} [col change])])

(defn table-component [changes hds tds]
  [table/container
   {:className "audited-changes"
    :actions [table/toolbar]
    :header
    [thead-component hds]
    :body
    (doall (for [change changes]
             (row-component change tds)))}])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div
     [:hr]
     [:div
      [:h3 "@tables*"]
      [:pre (with-out-str (pprint @tables*))]]
     [:div.data*
      [:h3 "@data"]
      [:pre (with-out-str (pprint @data*))]]]))

(defn page []
  [:article.audited-changes-page
   [:header.my-5
    [:h1 [icons/arrow-right-arrow-left] " Audited Changes "]]
   [:section
    [filter-component]
    [routing/hidden-state-component
     {:did-change fetch-changes}]
    (if-not (contains? @data* (:route @routing/state*))
      [wait-component]
      (if-let [changes (-> @data* (get (:route @routing/state*) {}) :changes seq)]
        [table-component changes]
        [:div.alert.alert-info.text-center "No (more) audited-changes found."]))
    [debug-component]]])
