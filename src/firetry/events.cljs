(ns firetry.events
  (:require
   [re-frame.core :as rf]
   [re-pressed.core :as rp]
   [firetry.db :as db]))


(defn simple-events [events-vec]
  (mapv (fn [event]
          (rf/reg-event-db event
                           (fn [db [_ value]]
                             (assoc db event value))))
        events-vec))

(simple-events [:firebase-db
                :firebase-app])

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-fx
  ::navigate
  (fn [_ [_ handler]]
   {:navigate handler}))

(rf/reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)
    :dispatch [::rp/set-keydown-rules
               {:event-keys [[[::set-re-pressed-example "Hello, world!"]
                              [{:keyCode 72} ;; h
                               {:keyCode 69} ;; e
                               {:keyCode 76} ;; l
                               {:keyCode 76} ;; l
                               {:keyCode 79}]]] ;; o

                :clear-keys
                [[{:keyCode 27}]]}]})) ;; escape


(rf/reg-event-db
 ::set-re-pressed-example
 (fn [db [_ value]]
   (assoc db :re-pressed-example value)))



(rf/reg-event-db
  ::load-data
  (fn [db [_ value]]
    (assoc db :loading-data true)))

(rf/reg-event-db
  ::save-data
  (fn [db [_ value]]
      (assoc db :saving-data true)))

(rf/reg-event-db
  ::clear-data
  (fn [db [_ value]]
      (dissoc db :data)))

(rf/reg-event-db
  ::set-data
  (fn [db [_ value]]
      (assoc db :data value)))

(rf/reg-event-db
  ::set-user
  (fn [db [_ user]]
      (assoc db :user user)))

(rf/reg-event-fx
  ::sign-in
  (fn [_ [_]]
    {:fx [[:sign-in-effect]]}))

(rf/reg-event-fx
  ::sign-out
  (fn [_ [_]]
    {:fx [[:sign-out-effect]]}))


