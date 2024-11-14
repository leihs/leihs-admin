(ns leihs.admin.resources.categories.category.image
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.admin.utils.image-resize :as image-resize]
   [reagent.core :as reagent]))

;;; image ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def img-processing* (reagent/atom {}))

(defn allow-drop [e] (.preventDefault e))

(defn get-img-data [dataTransfer array-buffer-handler]
  (js/console.log (clj->js ["TODO" 'get-img-data dataTransfer]))
  (let [url (.getData dataTransfer "text/plain")]
    (js/console.log (clj->js ["URL" url]))
    (js/console.log (clj->js ["DATA" (.getData dataTransfer "text/uri-list")]))
    (js/console.log (clj->js ["DATA" (.getData dataTransfer "text/html")]))
    (js/console.log (clj->js ["ITEMS" (.-items dataTransfer)]))
    (js/console.log (clj->js ["TYPES" (.-types dataTransfer)]))))

(defn get-file-data [dataTransfer array-buffer-handler data*]
  (let [f (aget (.-files dataTransfer) 0)
        fname (.-name f)
        reader (js/FileReader.)]
    (set! (.-onload reader)
          (fn [e]
            (let [data (-> e .-target .-result)]
              (array-buffer-handler data data*))))
    (.readAsArrayBuffer reader f)))

(defn img-handler [data data*]
  (image-resize/resize-to-b64
   data 512

   :error-handler
   (fn [err]
     (swap! img-processing* assoc :error err))

   :success-handler
   (fn [b64]
     (swap! data* assoc :image b64))))

(defn handle-img-drop [evt data*]
  (reset! img-processing* {})
  (allow-drop evt)
  (.stopPropagation evt)
  (let [data-transfer (.. evt -dataTransfer)]
    (if (< 0 (-> data-transfer .-files .-length))
      (get-file-data data-transfer img-handler data*)
      (get-img-data data-transfer img-handler))))

(defn handle-img-chosen [evt data*]
  (reset! img-processing* {})
  (get-file-data (-> evt .-target) img-handler data*))

(defn file-upload [data*]
  [:div.mb-2
   {:style {:position :relative}
    :on-drag-over #(allow-drop %)
    :on-drop #(handle-img-drop % data*)
    :on-drag-enter #(allow-drop %)}
   [:div
    {:style
     {:position :relative
      :display "flex"
      :width "100%"
      :aspect-ratio "1/1"
      :left 0
      :top 0}}
    (if-let [img-data (:image @data*)]
      [:img {:src img-data
             :style {:display :block
                     :object-fit "contain"
                     :opacity 0.4}}]
      [:div.bg-light
       {:style {:position :absolute
                :width "100%"
                :height "100%"
                :left 0
                :top 0}}])]
   [:div.text-center
    {:style
     {:position :absolute
      :width "100%"
      :height "100%"
      :top 0}}
    [:div.pt-2
     [:label.btn.btn-sm.btn-dark
      [:i.fas.fa-file-image]
      " Choose file "
      [:input#user-image.sr-only
       {:type :file
        :on-change #(handle-img-chosen % data*)}]]

     [:p "or drop file image here"]]
    [:div.text-center
     {:style {:position :absolute
              :bottom 0
              :width "100%"}}
     [:div
      (when (:image @data*)
        [:p {:style {:margin-top "1em"}}
         [:a.btn.btn-sm.btn-dark
          {:href "#"
           :on-click #(swap! data* assoc :image nil)}
          [:i.fas.fa-times] " Remove image "]])]]]])

(defn component [data*]
  [:div
   [file-upload data*]
   [:input
    {:id "image"
     :type "text"
     :hidden true
     :readOnly true
     :value (or (:image @data*) "")}]])
