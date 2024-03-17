(ns firetry.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [firetry.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 1))))
