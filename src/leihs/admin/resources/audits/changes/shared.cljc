(ns leihs.admin.resources.audits.changes.shared
  (:require
   [leihs.admin.constants :as defaults]))

(def default-query-params
  {:page 1
   :pkey ""
   :table ""
   :term ""
   :start-date ""
   :end-date ""
   :tg-op ""
   :per-page defaults/PER-PAGE})

