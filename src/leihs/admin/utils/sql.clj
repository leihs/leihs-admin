(ns leihs.admin.utils.sql
  (:require [honey.sql.helpers :as sql]))

(defn where-with-sanitized-uuid [query col uuid]
  (if (instance? java.util.UUID uuid)
    (sql/where query [:= col uuid])
    (sql/where query [:= true false])))

