(ns numberwords.core-test
  (:require [clojure.spec.test.alpha :as st]
            [clojure.test :refer [is deftest]]
            [numberwords.domain :as nd]
            [numberwords.core :as nw]))

(-> (st/enumerate-namespace 'numberwords.core) st/check)

(deftest hedge-identification
  (is (= ["exactly"] (nw/hedge :en ::nd/equal)))
  (is (= ["around" "approximately" "about"] (nw/hedge :en ::nd/around)))
  (is (= ["apie" "apytiksliai"] (nw/hedge :lt ::nd/around)))
  (is (= ["ungefähr" "etwa"] (nw/hedge :de ::nd/around)))
  (is (= ["cerca de" "aproximadamente"] (nw/hedge :pt ::nd/around))))

(deftest actual-values-at-extremes
  (is (= {::nd/equal 0} (nw/numeric-relations 0 1/4)))
  (is (= {::nd/equal 1} (nw/numeric-relations 1 1/100)))
  (is (= {::nd/equal 110} (nw/numeric-relations 110 10))))

(deftest given-values-at-favorite-numbers
  (is (= ["a half"] (nw/favorite-number :en 1/2)))
  (is (= ["ketvirtis"] (nw/favorite-number :lt 1/4))))

(deftest full-expression
  (is (= "around zero point two" (nw/numeric-expression :en 0.23 1/10 ::nd/around ::nd/words)))
  (is (= "around 0.2" (nw/numeric-expression :en 0.23 1/10 ::nd/around ::nd/numbers)))
  )
