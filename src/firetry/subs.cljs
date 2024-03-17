(ns firetry.subs
  (:require
   [re-frame.core :as re-frame]))

(defn simple-subs
  "For simple subscriptions, where subscription name and path in db are same,
   do it like this:
   (simple-subs [;; sub to :user key in db, sub name is :user
                 :user])
   Even for nested paths, do it like this:
   (simple-subs [;; sub to :time key in db, sub name is :time
                 :time
                 ;; sub to path [:user :display-name] in db, sub name is :display-name
                 [:display-name [:user :display-name]]]"
  [subs]
  (mapv (fn [sub] (if (vector? sub)
                    (re-frame/reg-sub
                      (get sub 0)
                      (fn [db]
                        (get-in db (get sub 1) #_(str "ERROR: path not found in db: " (get sub 1)))))
                    (re-frame/reg-sub
                      sub
                      (fn [db]
                        (sub db)))))
        subs))

(simple-subs [:user
              [:photo-url [:user :photo-url]]
              [:display-name [:user :display-name]]
              [:email [:user :email]]
              :firebase-db
              :firebase-app])

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::re-pressed-example
 (fn [db _]
   (:re-pressed-example db)))

(re-frame/reg-sub
  ::data
  (fn [db _]
    (:data db)))
