(ns leihs.admin.resources.categories.category.edit-image-resize
  (:require
   ["jimp" :refer [Jimp]]))

(defn resize-to-b64
  [data max-dimension & {:keys [error-handler success-handler]
                         :or {error-handler #()
                              success-handler #()}}]
  (-> (.read Jimp data)
      (.then (fn [^js img]
               (.resize img #js {:w max-dimension :h max-dimension})
               (.getBase64 img "image/jpeg" #js {:quality 80})))
      (.then success-handler)
      (.catch error-handler)))
