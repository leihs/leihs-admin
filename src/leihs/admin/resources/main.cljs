(ns leihs.admin.resources.main
  (:refer-clojure :exclude [str keyword]))

(defn page []
  [:article.admin.my-5
   [:div
    [:h1 "Admin"]
    [:p "The application to administrate this instance of "
     [:em " leihs"] "."]]])
