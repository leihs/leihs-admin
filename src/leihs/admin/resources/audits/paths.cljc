(ns leihs.admin.resources.audits.paths
  (:require
   [bidi.verbose :refer [branch leaf param]]))

(def paths
  (branch "/audited"
          (leaf "" :audits)
          (branch "/changes"
                  (leaf "/" :audited-changes)
                  (branch "/"
                          ; bidi doesn't like the exact match with ^...$ in leihs.admin.utils.regex here!
                          (param [#"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" :audited-change-id])
                          (leaf "" :audited-change)))
          (branch "/requests"
                  (leaf "/" :audited-requests)
                  (branch "/"
                          (param :request-id)
                          (leaf "" :audited-request)))
          (branch "/responses/"
                  (param :response-id)
                  (leaf "" :audited-responses))))
