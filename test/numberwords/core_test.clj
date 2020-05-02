(ns numberwords.core-test
  (:require [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [is are deftest]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [numberwords.core :refer [approximations] :as nw]))

(st/instrument `approximations)

(defspec check-approximations 200
  (prop/for-all
   [lang (s/gen :numwords/language)
    value (s/gen :numwords/actual-value)
    scale (s/gen :numwords/scale)]
   (let [result (approximations lang value scale)]
     (cond
       (= [:numwords/around] (keys result))
       ;;this branch is here to handle big numbers where
       ;;calcs become unreliable due to floating point operations
       (let [gv (get-in result [:numwords/around :numwords/given-value])]
         (is (or (< 1000 (/ gv scale))
                 (<= (Math/abs (double (- gv value)))
                     (* 2 (Math/ceil scale))))))

       (= [:numwords/equal] (keys result))
       (is (= (double value)
              (double 
               (get-in result [:numwords/equal :numwords/given-value]))))

       :else
       (is (<= (get-in result [:numwords/more-than :numwords/given-value])
               (rationalize value)
               (get-in result [:numwords/less-than :numwords/given-value])))))))

(defn given-vals [result]
  [(get-in result [:numwords/around :numwords/given-value])
   (get-in result [:numwords/less-than :numwords/given-value])
   (get-in result [:numwords/more-than :numwords/given-value])])

(defn fav-nums [result]
  [(get-in result [:numwords/around :numwords/favorite-number])
   (get-in result [:numwords/less-than :numwords/favorite-number])
   (get-in result [:numwords/more-than :numwords/favorite-number])])

(defn hedges [result]
  [(get-in result [:numwords/equal :numwords/hedges])
   (get-in result [:numwords/around :numwords/hedges])
   (get-in result [:numwords/less-than :numwords/hedges])
   (get-in result [:numwords/more-than :numwords/hedges])])

(deftest hedge-identification
  (are [result lang value step] (= result (hedges (approximations lang value step)))
    [#{"exactly"} nil nil nil]
    :en 0 1/4

    [nil #{"around" "approximately" "about"} #{"less than" "under" "nearly"} #{"more than" "over"}]
    :en 0.54M 1/10

    [nil #{"apie" "apytiksliai"} #{"mažiau nei" "mažiau"} #{"virš" "daugiau nei"}]
    :lt 0.54M 1/10

    [nil #{"etwa" "ungefähr"} #{"unter" "weniger als"} #{"über" "mehr als"}]
    :de 0.54M 1/10

    [nil #{"cerca de" "aproximadamente"} #{"menos de" "abaixo de"} #{"mais de" "acima de"}]
    :pt 0.54M 1/10))

(deftest actual-values-at-extremes
  (are [result lang value step] (= result (get-in (approximations lang value step)
                                                  [:numwords/equal :numwords/given-value]))
    0   :en 0   1/4
    1   :en 1   1/100
    110 :en 110 10))

(deftest given-values-at-favorite-numbers
  (are [result lang value step] (= result (given-vals (approximations lang value step)))
    [1/2 3/5 1/2] :en 0.54M 1/10
    [0 1/4 0]     :en 1/10  1/4
    [0 1/4 0]     :en 0.1M  1/4)

  (are [result lang value step] (= result (fav-nums (approximations lang value step)))
    [#{"a half"} nil #{"a half"}] :en 0.54M 1/10
    [nil #{"a quarter"} nil]      :en 1/10  1/4
    [nil #{"a quarter"} nil]      :en 0.1M  1/4
    [nil #{"ketvirtis"} nil]      :lt 0.1M  1/4
    [nil #{"Viertel"} nil]      :de 0.1M  1/4
    [nil #{"um quarto"} nil]      :pt 0.1M  1/4))

(deftest non-special-given-values
  (are [result lang value step] (= result (given-vals (approximations lang value step)))
    [3/4 1 3/4]     :en 0.8  1/4
    [1/10 1/5 1/10] :en 0.14 1/10
    [50 60 50]      :en 54   10))
