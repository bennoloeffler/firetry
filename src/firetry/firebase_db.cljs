(ns firetry.firebase-db
  (:require [reagent.core :as r]
            [reagent.ratom :as ra]
            ["firebase/firestore" :as fs :refer [onSnapshot getFirestore doc getDoc setDoc addDoc updateDoc deleteDoc collection, query, where, getDocs]]
            [firetry.utils :refer [bean]]
            [promesa.core :as p]))


;; RE-FRAME and FIREBASE without firebase wrapper
; https://widdindustries.com/blog/clojurescript-firebase-simple
; https://github.com/henryw374/firebase-clojurescript-todo-list

;; REAGENT reactions
; https://cljdoc.org/d/reagent/reagent/1.2.0/doc/tutorials/-wip-managing-state-atoms-cursors-reactions-and-tracking

;; FIREBASE listening
; https://firebase.google.com/docs/firestore/query-data/listen

(defn db [] (getFirestore))

(defn get-doc
  "Get path to document. This is a local operation.
  It does not fetch the document from the server
  and works even if the document does not exist."
  [path]
  (doc (db) path))

(defn get-col [path]
  (collection (db) path))

(comment
  ;; get document reference - even without a users/1 existing
  (bean (get-doc "users/1")))


(defn store-doc
  "Create data at path and delete existing data if any."
  [path data]
  (setDoc (get-doc path)
          (clj->js data)))

(defn store-doc-new-id [path data] (addDoc (get-col path) (clj->js data)))

(defn update-doc
  "Add (merge) data to path."
  [path data]
  (updateDoc (get-doc path)
             (clj->js data)))

(comment
  ;; store document
  (store-doc "users/2" {:name "John Doe"})

  ;; update document
  (update-doc "users/2" {:age 45 :language "english"})
  (update-doc "users/2" {:language "spanish"}))


(defn fetch-doc
  "Fetch data at path."
  [path on-success #_on-failure]
  (-> (getDoc (get-doc path))
      (p/then (fn [doc] (on-success (bean (.data doc)))))
      #_(p/catch (fn [doc] (on-failure (bean doc))))))

(comment
  ;; read
  (fetch-doc "users/2"
             (fn [data] (println "success: " data))
             #_(fn [data] (println "failure: " data))))


(defn delete-doc
  "Delete data at path - including the id. Subcollections with paths
  are NOT deleted. They have to be deleted separately."
  [path]
  (deleteDoc (get-doc path)))

(comment
  ;; delete
  (delete-doc "users/2"))


(defn <sub-fb
  "When used in reagent component,
  it will subscribe to the data at path
  and re-render the component when data changes.
  CAUTION: use form2 subcription!
  Example:
  (defn debug-panel []
    (let [user2 (<sub-fb \"users/2\")]
      (fn []
       [:div (str \"<sub-fb: \" @user2)])))"
  [path]
  (let [ref      ^js (get-doc path)
        val      (r/atom nil)
        callback (fn [x] (let [value (bean (.data x))]
                           ;(println "<sub-fb, path: " path ", value: " value)
                           (reset! val value)))
        dispose  (onSnapshot ref callback)]
    (ra/make-reaction
      (fn [] #_(println "reading reaction, val: " @val) @val)
      :on-dispose (fn [] (dispose)))))

(comment
  ;; test notification functions of firestore
  (def ref (get-doc "users/2"))
  (def cb (fn [x] (println "callback, value: " x)))
  (def dispose (onSnapshot ref cb))
  (dispose)

  ;; use reaction as subscription:
  ;; CAUTION: in reagent component, use form 2 subscription
  ;; otherwise, it will be endlessly re-rendered
  (def reaction (<sub-fb "users/2"))
  @reaction)

(defn <sub-fb-col
  "When used in reagent component,
  it will subscribe to the data resulting from the query
  and re-render the component when data changes.
  CAUTION: use form2 subcription!
  Example:
  (defn debug-panel []
    (let [users (<sub-fb-col \"users\")]
      (fn []
       [:div (str \"<sub-fb-col: \" @users)])))"
  [query]
  (let [val      (r/atom {})
        callback (fn [query-Snapshot] (.forEach query-Snapshot
                                                (fn [doc]
                                                  (let [value (bean (.data doc))]
                                                    ;(println "<sub-fb, path: " path ", value: " value)
                                                    (swap! val merge {(.-id doc)
                                                                      value})))))
        dispose  (onSnapshot query callback)]
    (ra/make-reaction
      (fn [] #_(println "reading reaction, val: " @val) @val)
      :on-dispose (fn [] (dispose)))))

(defn <sub-fb-col-all [path]
  (<sub-fb-col (query (get-col path))))

(comment
  ;; test notification functions of firestore
  (def q (query (get-col "users")
                (where "languages" "array-contains" "spanish")))
  (type q)
  (def cb (fn [x] (println "callback, value: " (.forEach x (fn [doc] (println "doc: " (bean (.data doc))))))))
  (def dispose (onSnapshot q cb))
  (dispose)



  ;; use reaction as subscription:
  ;; CAUTION: in reagent component, use form 2 subscription
  ;; otherwise, it will be endlessly re-rendered
  (def reaction (<sub-fb-col (query (get-col "users")
                                    (where "languages" "array-contains" "spanish"))))
  @reaction)


(defn fetch-docs
  "Fetch all documents from a query.
  Returns a r/atom containing the result later - for reagent components.
  Or, if a callback f is given, it will be called with the result as argument."
  ([query]
   (fetch-docs query nil))
  ([query f]
   (let [final-result-vec (r/atom ["fetching..."])]
      (-> (getDocs query)
          (p/then (fn [query-snapshot]
                     (reset! final-result-vec [])
                     (.forEach query-snapshot
                       (fn [doc]
                         ;(println "doc: " (bean (.data doc)))
                         (let [doc-data (.data doc)]
                           (when doc-data
                             ;(println (str (.-id doc) " => " (bean doc-data)))
                             (swap! final-result-vec conj (bean doc-data))))))
                     (when f (f @final-result-vec)))));(println "final-result-vec: " @final-result-vec)
      final-result-vec)))

(comment

  ;; add data for testing queries
  (do
    (store-doc "users/sabine" {:name "Sabine" :born 1971 :languages ["german" "english" "spanish"]})
    (store-doc "users/benno" {:name "Benno" :born 1969 :languages ["german" "english"]})
    (store-doc "users/benno-jun" {:name "Benno jun" :born 2000 :languages ["german" "english" "spanish"]})
    (store-doc "users/paul" {:name "Paul" :born 2002 :languages ["german" "english" "spanish"]})
    (store-doc "users/leo" {:name "Leo" :born 2006 :languages ["german" "english"]}))

  (store-doc-new-id "users" {:name "Franz" :born 1951 :languages ["german" "english" "spanish"]})


  ;;  queries
  (do (def users (collection (db) "users"))
      (def q (query users (where "name" "==" "Benno")))
      ;(def q (query users (where "born" "<" 2000)))
      (def queryResults (getDocs q))
      (.then queryResults (fn [querySnapshot]
                            (.forEach querySnapshot
                                      (fn [doc]
                                        (println "doc: "
                                                 (bean (.data doc))))))))

  (def result (fetch-docs (query (collection (db) "users")
                                 ; DOES NOT WORK, see
                                 ; https://firebase.google.com/docs/firestore/query-data/queries
                                 (where "name" "!=" "Benno")
                                 (where "born" ">=" 2000))
                          #(println "callback: " %)))
  @result

  (def result (fetch-docs (query (collection (db) "users")
                               ; WORKS, see
                               ; https://firebase.google.com/docs/firestore/query-data/queries
                               (where "born" "==" 2000); BUT NOT < > <= >=
                               (where "languages" "array-contains" "spanish"))
                        #(println "callback: " %)))
  @result



  ;; reagent r/atom
  (let [result (fetch-docs (query (collection (db) "users")
                                  (where "born" "<" 2000)))]
    (js/setTimeout #(println "after waited 3: " @result) 3) ;; dont wait until result is available
    (js/setTimeout #(println "after waited 300: " @result) 300) ;; wait until result is available
    (type result))

  ;; callback only
  (let [_ (fetch-docs (query (collection (db) "users")
                             (where "born" "<" 2000))
                      (fn [result] (println "callback: " result)))])


  ;; callback and r/atom
  (let [result (fetch-docs (query (collection (db) "users")
                                  (where "born" "<" 2000))
                           (fn [result] (println "callback: " result)))]
    (js/setTimeout #(println "after waited 0: " @result) 0) ;; dont wait until result is available
    (js/setTimeout #(println "after waited 300: " @result) 300)) ;; wait until result is available

  nil)