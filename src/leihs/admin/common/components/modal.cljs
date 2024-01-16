(ns leihs.admin.common.components.modal
  (:require
   [react-bootstrap :as react-bootstrap :refer [Modal Button]]))

(defn centered
  [props]
  (let [size "lg"
        aria-labelledby "contained-modal-title-vcenter"]
    [:> Modal
     (assoc props :size size
            :aria-labelledby aria-labelledby
            :centered true)
     [:> Modal.Header {:closeButton true
                       :onHide (get props :onHide)}
      [:> Modal.Title {:id aria-labelledby}
       (get props :title)]]
     [:> Modal.Body
      (get props :body)]
     [:> Modal.Footer
      (if (get props :footer)
        (get props :footer)
        [:> Button {:onClick (get props :onHide)}
         "Close"])]]))
