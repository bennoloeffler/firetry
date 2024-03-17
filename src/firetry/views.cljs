(ns firetry.views
  (:require
    [re-frame.core :as re-frame]
    [firetry.utils :refer [>evt <sub bean]]
    [firetry.firebase-db :refer [<sub-fb <sub-fb-col <sub-fb-col-all]]
    [breaking-point.core :as bp]
    [firetry.config :as config]
    [firetry.events :as events]
    [firetry.routes :as routes]
    [firetry.subs :as subs]))


;; home

(defn display-re-pressed-example []
  (let [re-pressed-example (re-frame/subscribe [::subs/re-pressed-example])]
    [:div

     [:p
      [:span "Re-pressed is listening. type"]
      [:strong [:code " h e l l o "]]]

     (when-let [rpe @re-pressed-example]
       [:div
        {:style {:padding          "16px"
                 :background-color "lightgrey"
                 :border           "solid 1px grey"
                 :border-radius    "4px"
                 :margin-top       "16px"}}

        rpe])]))


(defn data-panel []
  (let [data (re-frame/subscribe [::subs/data])]

    [:div.section>div.container
     [:h1.title "This is the Data Page."]
     [:h1.subtitle "put data here. will be read as clj edn data structure"]
     [:div.block
      [:a {:on-click #(re-frame/dispatch [::events/navigate :home])}
       "go to Home Page"]]
     [:div.block

      [:textarea.textarea
       {:placeholder "put your clojure-data here.\ne.g. \n[{:name \"Benno\" :age 42}\n {:name \"Hans\" :age 43}]"
        :value       @data
        :on-change   #(re-frame/dispatch [::events/set-data (-> % .-target .-value)])}]
      [:br]

      [:div.button
       {:on-click #(re-frame/dispatch [::events/set-data "[:this :is :example :data]"])}
       "put example data"]
      [:div.button
       {:on-click #(re-frame/dispatch [::events/clear-data])}
       "clear"]
      [:div.button
       {:on-click #(re-frame/dispatch [::events/save-data])}
       "save"]
      [:div.button
       {:on-click #(re-frame/dispatch [::events/load-data])}
       "load"]
      [:br] [:br] [:br]]]))

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div.section
     [:h1.title @name]
     [:h1.subtitle "...trying auth and db in firebase"]
     [:div.button
      {:on-click #(re-frame/dispatch [::events/sign-in])}
      "login with google"]
     [:div.button
      {:on-click #(re-frame/dispatch [::events/sign-out])}
      "logout"
      [:img.ml-3 {:src   (<sub [:photo-url])
                  :style {:border-radius "30%" :height "30px"}}]]


     [:pre (str "email: " @(re-frame/subscribe [:email]) "\n"
                "display-name: " @(re-frame/subscribe [:display-name]))]
     [:img {:src (<sub [:photo-url])}]



     [:div.label "navigation"]
     [:div
      [:a {:on-click #(re-frame/dispatch [::events/navigate :data])}
       "data"]]
     [:div
      [:a {:on-click #(re-frame/dispatch [::events/navigate :debug])}
       "debug"]]
     [:br]]))



(defmethod routes/panels :home-panel [] [home-panel])



(defmethod routes/panels :data-panel [] [data-panel])

;; bel


(defn debug-panel []
  (let [users (<sub-fb-col-all "users")
        user  (<sub-fb "users/benno")]
    (fn []
      [:div.section
       [:h1.title "This is the DEBUG Page."]
       [:div.subtitle
        [:a {:on-click #(re-frame/dispatch [::events/navigate :home])}
         "go to Home Page"]]
       [:div.box (str "git tag: " config/version)] [:br]
       #_[:div.box [display-re-pressed-example]]
       [:div.box [:b "subscription (<sub-fb \"user\") "] @user]
       [:div.box [:b "subscription (<sub-fb-col \"users\") "]
        [:ul (map (fn [[k v]] ^{:key k} [:li  (str (:name v) ", " (or (:born v) "birth unknown") ", id: " k)])
                  @users)]]

       [:div.box
        [:h3 (str "screen-width: " @(re-frame/subscribe [::bp/screen-width]))]
        [:h3 (str "screen: " @(re-frame/subscribe [::bp/screen]))]]

       [:div.box
        [:h3 (str "screen-width: " @(re-frame/subscribe [::bp/screen-width]))]
        [:h3 (str "screen: " @(re-frame/subscribe [::bp/screen]))]]])))



(defmethod routes/panels :debug-panel [] [debug-panel])


;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))

(comment
  (+ 1 1))
  
