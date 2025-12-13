(ns leihs.admin.resources.settings.paths
  (:require
   [bidi.verbose :refer [branch leaf param]]))

(def paths
  (branch "/settings/"
          (leaf "" :settings)
          (leaf "languages/" :languages-settings)
          (leaf "system-and-security/" :system-and-security-settings)
          (leaf "misc/" :misc-settings)
          (branch "smtp/"
                  (leaf "" :smtp-settings)
                  (leaf "emails" :smtp-emails)
                  (leaf "test-email" :smtp-test-email)
                  (leaf "ms365-callback" :smtp-ms365-callback)
                  (branch "ms365-mailboxes"
                          (leaf "/" :smtp-ms365-mailboxes)
                          (branch "/"
                                  (param [#"[^/]+" :mailbox-id])
                                  (leaf "" :smtp-ms365-mailbox))))
          (leaf "syssec/" :syssec-settings)))
