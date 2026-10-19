(ns leihs.admin.utils.image-resize
  (:require
   ["jimp" :refer [Jimp]]
   [clojure.string :as str]))

(defn base64-size
  [b64-string]
  (let [b64-string (if (str/includes? b64-string ",")
                     (second (str/split b64-string #","))
                     b64-string)
        padding (count (re-seq #"=" b64-string))
        size-in-bytes (/ (* (count b64-string) 3) 4)]
    (- size-in-bytes padding)))

(defn raw-data
  [b64-string]
  (if (str/includes? b64-string ",")
    (second (str/split b64-string #","))
    b64-string))

(defn resize-to-b64
  [data max-dimension & {:keys [error-handler success-handler]
                         :or {error-handler #()
                              success-handler #()}}]
  (-> (.read Jimp data)
      (.then (fn [^js img]
               (.scaleToFit img #js {:w max-dimension :h max-dimension})
               (let [width (.-width img)
                     height (.-height img)
                     content-type "image/jpeg"]
                 (-> (.getBase64 img content-type #js {:quality 80})
                     (.then (fn [b64]
                              (success-handler b64 (raw-data b64) width height content-type)))))))
      (.catch error-handler)))
