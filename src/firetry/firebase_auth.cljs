(ns firetry.firebase-auth
  (:require
    [re-frame.core :as rf]
    [firetry.events :as events]
    [firetry.utils :refer [>evt <sub ->clj-map]]
    ["firebase/app" :as firebase]
    ["firebase/auth" :as auth :refer [GoogleAuthProvider getAuth signInWithPopup signOut onAuthStateChanged]]
    ["firebase/firestore/lite" :as f :refer [getFirestore]]))

; use Firestore Database. NOT Realtime Database

;(defonce user-global (atom nil))
(def fire (atom {:app nil :db nil}))



(defn select-js-keys
  "Selects keys from a js object in form of transformed kebab-case-keywords
  example:

  (select-js-keys
   {:email \"test\"
    :displayName \"test\"}
   [:email :display-name])

  => {:email \"test\" :display-name \"test\"}"
  [obj keys-vector]
  (select-keys (->clj-map obj)
               keys-vector))


(defn store-user [user-js]
  ;(reset! user-global user-js)
  (let [user (->clj-map user-js) #_(select-js-keys user-js [:email :display-name :uid])]
    (rf/dispatch [::events/set-user user])))


(defn delete-user []
  ;(println "delete-user")
  ;(reset! user-global nil)
  (rf/dispatch [::events/set-user nil]))


(defn do-init []
  (let
    ; secret data here...
    [app (firebase/initializeApp #js {:todo "add your firebase config here"})
     db  (getFirestore app)]
    (reset! fire {:app app :db db})
    (onAuthStateChanged (getAuth)
                        (fn [user] (if user
                                     (store-user user)
                                     (delete-user))))
    (>evt [:firebase-app app])
    (>evt [:firebase-db db])
    (println "initialized firebase app and db")))

(defn init []
  (if (= 0 (alength (.getApps firebase)))
    (do-init)
    (println "firebase already initialized")))


(defn sign-out []
  (let [auth (getAuth)]
    (signOut auth)))


(rf/reg-fx
  :sign-out-effect
  (fn [db [_]]
    (sign-out)))


(defn sign-in []
  (let [gap  (GoogleAuthProvider.)
        auth (getAuth)]
    (signInWithPopup auth gap)))


(rf/reg-fx
  :sign-in-effect
  (fn [db [_]]
    (sign-in)))


(comment
  (println auth)
  ;(js/console.log @user-global)
  (do-init)
  (sign-in)
  (sign-out))


