(ns leihs.admin.resources.categories.category.image
  (:refer-clojure :exclude [str keyword])
  (:require
   [cljs-http.client :as http]
   [cljs.core.async :refer [<! go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.object :as gobj]

   [leihs.admin.utils.image-resize :as image-resize]
   [reagent.core :as reagent :refer [reaction]]))

;;; atoms ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce data* (reagent/atom nil))

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

(defn get-file-data [dataTransfer array-buffer-handler]
  (let [f (aget (.-files dataTransfer) 0)
        fname (.-name f)
        reader (js/FileReader.)]
    (set! (.-onload reader)
          (fn [e]
            (let [data (-> e .-target .-result)]
              (array-buffer-handler data fname))))
    (.readAsArrayBuffer reader f)))

(defn img-handler [data filename]
  (image-resize/resize-to-b64
   data 32

   :error-handler
   (fn [err]
     (swap! img-processing* assoc :error err))

   :success-handler
   (fn [b64 raw width height content-type]
     (swap! data* assoc :thumbnail {:url b64
                                    :data raw
                                    :filename filename
                                    :content_type content-type
                                    :width width
                                    :height height})))
  (image-resize/resize-to-b64
   data 512

   :error-handler
   (fn [err]
     (swap! img-processing* assoc :error err))

   :success-handler
   (fn [b64 raw width height content-type]
     (swap! data* assoc :image {:url b64
                                :data raw
                                :filename filename
                                :content_type content-type
                                :width width
                                :height height}))))

(defn handle-img-drop [evt]
  (reset! img-processing* {})
  (allow-drop evt)
  (.stopPropagation evt)
  (let [data-transfer (.. evt -dataTransfer)]
    (if (< 0 (-> data-transfer .-files .-length))
      (get-file-data data-transfer img-handler)
      (get-img-data data-transfer img-handler))))

(defn handle-img-chosen [evt]
  (reset! img-processing* {})
  (get-file-data (-> evt .-target) img-handler))

(defn file-upload []
  [:div.mb-2
   {:style {:position :relative}
    :on-drag-over #(allow-drop %)
    :on-drop #(handle-img-drop %)
    :on-drag-enter #(allow-drop %)}
   [:div
    {:style
     {:position :relative
      :display "flex"
      :width "100%"
      :aspect-ratio "1/1"
      :left 0
      :top 0}}
    (if-let [img-data (:url (:image @data*))]
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
        :on-change #(handle-img-chosen %)}]]

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
           :on-click (fn []
                       (-> js/document
                           (.getElementById "user-image")
                           (.-value)
                           (set! ""))
                       (reset! data* nil))}
          [:i.fas.fa-times] " Remove image "]])]]]])

(defn component
  [inital-data*]

  (reagent/create-class
   {:component-did-mount
    (fn []
      (when @inital-data*
        (swap! data*
               assoc :image
               {:url (if (:metadata @inital-data*)
                       (-> @inital-data* :metadata :image_url)
                       (:image @inital-data*))})))

    :reagent-render
    (fn []
      [file-upload])}))
