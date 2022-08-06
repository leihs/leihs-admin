(ns leihs.admin.common.icons
  (:refer-clojure :exclude [next])
  (:require
    ["@fortawesome/react-fontawesome" :as fa-react-fontawesome :refer [FontAwesomeIcon]]
    ["@fortawesome/free-solid-svg-icons" :as solids]
    ["@fortawesome/free-brands-svg-icons" :as brands]
    ))


(defn add [] (FontAwesomeIcon #js{:icon solids/faPlusCircle :className ""}))
(defn admin [] (FontAwesomeIcon #js{:icon solids/faWrench :className ""}))
(defn authentication-system [] (FontAwesomeIcon #js{:icon solids/faExternalLinkAlt :className ""}))
(defn authentication-systems [] (FontAwesomeIcon #js{:icon solids/faExternalLinkAlt :className ""}))
(defn delegation [] (FontAwesomeIcon #js{:icon solids/faHandsHelping :className ""}))
(defn delegations [] (FontAwesomeIcon #js{:icon solids/faHandsHelping :className ""}))
(defn delete [] (FontAwesomeIcon #js{:icon solids/faTimes :className ""}))
(defn edit [] (FontAwesomeIcon #js{:icon solids/faEdit :className ""}))
(defn email [] (FontAwesomeIcon #js{:icon solids/faEnvelope :className ""}))
(defn entitlement-groups [] (FontAwesomeIcon #js{:icon solids/faObjectGroup :className ""}))
(defn github [] (FontAwesomeIcon #js{:icon brands/faGithubSquare :className ""}))
(defn group [] (FontAwesomeIcon #js{:icon solids/faUsers :className ""}))
(defn groups [] (FontAwesomeIcon #js{:icon solids/faUsers :className ""}))
(defn home [] (FontAwesomeIcon #js{:icon solids/faHome :className ""}))
(defn inventory [] (FontAwesomeIcon #js{:icon solids/faCube :className ""}))
(defn inventory-pool [] (FontAwesomeIcon #js{:icon solids/faCube :className ""}))
(defn inventory-pools [] (FontAwesomeIcon #js{:icon solids/faCubes :className ""}))
(defn password-reset [] (FontAwesomeIcon #js{:icon solids/faKey :className ""}))
(defn save [] (FontAwesomeIcon #js{:icon solids/faSave :className ""}))
(defn system [] (FontAwesomeIcon #js{:icon solids/faUserServer :className ""}))
(defn system-admin [] (FontAwesomeIcon #js{:icon solids/faUserAstronaut :className ""}))
(defn system-admins [] (FontAwesomeIcon #js{:icon solids/faUserAstronaut :className ""}))
(defn user [] (FontAwesomeIcon #js{:icon solids/faUser :className ""}))
(defn users [] (FontAwesomeIcon #js{:icon solids/faUserFriends :className ""}))
(defn view [] (FontAwesomeIcon #js{:icon solids/faEye :className ""}))
(defn waiting [& {:keys [size] :or {size "1x"}}] (FontAwesomeIcon #js{:icon solids/faCircleNotch :className "" :spin true :size size}))



