(ns leihs.admin.utils.image-resize
  (:require
   ["@jimp/custom"]
   ["jimp" :as Jimp]
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
  (.read Jimp data
         (fn [err ^js img]
           (if err
             (error-handler err)
             (do (doto img
                   (.scaleToFit max-dimension max-dimension)
                   (.quality 80))
                 (let [width (.getWidth img)
                       height (.getHeight img)
                       content-type "image/jpeg"]

                   (.getBase64 img content-type
                               (fn [err b64]
                                 (if err
                                   (error-handler err)
                                   (success-handler b64 (raw-data b64) width height content-type))))))))))
