(ns firetry.utils
  (:require
   [re-frame.core :as rf]
   [cljs-bean.core]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]))

(defn bean [js-obj] (cljs-bean.core/bean js-obj :recursive true))
(def ->clj cljs-bean.core/->clj)
(def ->js cljs-bean.core/->js)

(defn ->clj-map
  "Converts a js object recursively to a clojure map with kebab-case keywords."
  [js-obj]
  (cske/transform-keys
    csk/->kebab-case-keyword
    (bean js-obj)))

(def >evt rf/dispatch)
(def <sub (comp deref rf/subscribe))

