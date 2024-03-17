(ns firetry.core
  (:require
    [reagent.dom :as rdom]
    [re-frame.core :as re-frame]
    [re-pressed.core :as rp]
    [breaking-point.core :as bp]
    [firetry.events :as events]
    [firetry.routes :as routes]
    [firetry.views :as views]
    [firetry.config :as config]
    [firetry.firebase-auth :as firebase]))



(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (re-frame/dispatch-sync [::bp/set-breakpoints
                           {:breakpoints [:mobile
                                          768
                                          :tablet
                                          992
                                          :small-monitor
                                          1200
                                          :large-monitor]
                            :debounce-ms 166}])
  (dev-setup)
  (firebase/init)
  (mount-root))
