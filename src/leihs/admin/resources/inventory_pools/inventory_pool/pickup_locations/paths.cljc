(ns leihs.admin.resources.inventory-pools.inventory-pool.pickup-locations.paths
  (:require
   [bidi.verbose :refer [branch param leaf]]))

(def paths
  (branch "/pickup-locations"
          (branch "/"
                  (leaf "" :inventory-pool-pickup-locations))
          (branch "/"
                  (param :pickup-location-id)
                  (leaf "" :inventory-pool-pickup-location))))
