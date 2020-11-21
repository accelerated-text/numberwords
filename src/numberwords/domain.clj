(ns numberwords.domain
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [numberwords.config :as cfg]
            [numberwords.approx-math :as math]))

;;the value for which numeric expression is to be calculated
(s/def ::actual-value (s/and number? math/nat-num? math/not-inf?))

;;textual expression of the number
;; (s/def ::text (s/and string? #(not (string/blank? %))))
;;textual hedge describing the relation between given and actual value
(s/def ::hedges (s/coll-of string? :kind set?))
;;given value - the value given by the numeric expression calculation as the one
;;rounding the actual value
(s/def ::given-value (s/and number? math/nat-num?))
;;in case there are favorite expressions for a given number, spell it out
(s/def ::favorite-number (s/coll-of string? :kind set?))

(s/def ::relation #{::around ::more ::less :equal})

;;Given value relation to the actual value - a number on a scale grid
;; :equal in case actual value is equal to given value
;; :unreliable will be provided when working with huge numbers and where scale is
;;             larger than actual value
;; :unequal main case when we will have all three numeric expressions generated
(s/def ::given-value-relations
  (s/or :equal      (s/map-of #{::equal} ::given-value)
        :unreliable (s/map-of #{::around} ::given-value)
        :unequal    (s/map-of #{::around ::more ::less}
                              ::given-value :min-count 3)))

;;rounding (snapping) scale to use when calculating values which will be
;;provided as numeric expressions
(s/def ::scale (s/or :fraction    (s/and ratio? #(and (> % 0)
                                                              (> (denominator %)
                                                                 (numerator %))))
                             :natural-num (s/and number? pos-int?)))

;;supported languages
(s/def ::language (cfg/supported-langauges))
