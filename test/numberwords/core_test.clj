(ns numberwords.core-test
  (:require [clojure.spec.test.alpha :as st]
            [clojure.test :refer [deftest are]]
            [numberwords.domain :as nd]
            [numberwords.core :as nw]))

(-> (st/enumerate-namespace 'numberwords.core) st/check)

(deftest hedge-identification
  (are [result relation language] (= result (nw/hedge relation language))
    ["exactly"]                        ::nd/equal  :en
    ["around" "approximately" "about"] ::nd/around :en
    ["apie" "apytiksliai"]             ::nd/around :lt
    ["ungefähr" "etwa"]                ::nd/around :de
    ["cerca de" "aproximadamente"]     ::nd/around :pt))

(deftest actual-values-at-extremes
  (are [result value scale] (= result (nw/numeric-relations value scale))
    {::nd/equal 0}   0   1/4
    {::nd/equal 1}   1   1/100
    {::nd/equal 110} 110 10))

(deftest given-values-at-favorite-numbers
  (are [result value language] (= result (nw/favorite-number value language))
    ["a half"]    1/2 :en
    ["ketvirtis"] 1/4 :lt))

(deftest possible-relations-in-prefered-order
  (are [result value scale relation]
      (= result (-> value
                    (nw/numeric-relations scale)
                    (nw/possible-relation relation)))
    ::nd/equal 120 10 ::nd/less
    ::nd/less  122 10 ::nd/less))

(deftest full-expression
  (are [result actual-value language scale relation formatting]
      (= result
         (nw/numeric-expression actual-value language scale relation formatting))
    "around zero point two" 0.23 1/10 :en ::nd/around ::nd/words
    "around 0.2"            0.23 1/10 :en ::nd/around ::nd/numbers))
