(ns firetry.http
  (:require [firetry.utils :refer [bean]]
            [clojure.pprint :refer [pprint]]
            [shadow.cljs.modern :refer (js-await)]
            [promesa.core :as p]))

; ----- vanilla js interop ---------
; println in promise callback does not print to repl!

(defn dbg [x] (println "dbg: " x) x)
(defn http-json [succ-fn err-fn]
  (let [p (js/fetch "https://jsonplaceholder.typicode.com/todos/1")]
    (-> p
        (.then (fn [response] (.json response)))
        (.then (fn [json] (succ-fn (bean json))))
        (.catch (fn [err] (err-fn err))))))

(comment
  (http-json (fn [data] (println "final: " (with-out-str (pprint data))))
             (fn [err] (println "error: " err))))

; ------ shadow macro -----------
; println in promise callback does not print to repl!

(defn my-fetch-fn []
  (js-await [res (js/fetch "https://jsonplaceholder.typicode.com/todos/1")]
            (js/console.log "res" res)
            (js-await [body (.json res)]
                      (println "got some json" body))))
(comment
  (my-fetch-fn))


; ------ shadow macro -----------
; println in promise callback does not print to repl!

(defn return-p [] (p/let [x (p/delay 1000 :hello)]
                         (p/then x (println "then: " x))))
(comment
  (return-p))
