(ns leihs.admin.resources.settings.paths
  (:require
   [bidi.verbose :refer [branch leaf]]))

(def paths
  (branch "/settings/"
          (leaf "" :settings)
          (leaf "languages/" :languages-settings)
          (leaf "system-and-security/" :system-and-security-settings)
          (leaf "misc/" :misc-settings)
          (branch "smtp/"
                  (leaf "" :smtp-settings)
                  (leaf "emails" :smtp-emails)
                  (leaf "test-email" :smtp-test-email))
          (leaf "syssec/" :syssec-settings)))
