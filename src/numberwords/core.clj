(ns numberwords.core
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [numberwords.number-ops :as no]
            [numberwords.config :as cfg]
            [numberwords.text :as text]))

;;the value for which numeric expression is to be calculated
(s/def :numwords/actual-value (s/and number? no/nat-num? no/not-inf?))

;;textual expression of the number
(s/def :numwords/text (s/and string? #(not (string/blank? %))))
;;textual hedge describing the relation between given and actual value
(s/def :numwords/hedges (s/coll-of string? :kind set?))
;;given value - the value given by the numeric expression calculation as the one
;;rounding the actual value
(s/def :numwords/given-value (s/and number? no/nat-num?))
;;in case there are favorite expressions for a given number, spell it out
(s/def :numwords/favorite-number (s/coll-of string? :kind set?))

(s/def :numwords/relation #{:numwords/around :numwords/more :numwords/lest :equal})

;;Given value relation to the actual value - a number on a scale grid
;; :equal in case actual value is equal to given value
;; :unreliable will be provided when working with huge numbers and where scale is
;;             larger than actual value
;; :unequal main case when we will have all three numeric expressions generated
(s/def :numwords/given-value-relations
  (s/or :equal      (s/map-of #{:numwords/equal} :numwords/given-value)
        :unreliable (s/map-of #{:numwords/around} :numwords/given-value)
        :unequal    (s/map-of #{:numwords/around :numwords/more :numwords/less}
                              :numwords/given-value :min-count 3)))

;;rounding (snapping) scale to use when calculating values which will be
;;provided as numeric expressions
(s/def :numwords/scale (s/or :fraction    (s/and ratio? #(and (> % 0)
                                                              (> (denominator %)
                                                                 (numerator %))))
                             :natural-num (s/and number? pos-int?)))

;;supported languages
(s/def :numwords/language (cfg/supported-langauges))

(defn distances-from-edges
  [actual-value [start end]]
  [[start (no/delta start actual-value)]
   [end (no/delta end actual-value)]])

(defn unreliable?
  "Actual value and scale values which will generate unreliable results:
  - actual-value much bigger that the scale
  - scale is bigger than the actual value"
  [actual-value scale]
  ;; 1000x difference between the scale and value is a random choice
  (or (< 1000 (/ actual-value scale))
      (and (< 1 scale) (< actual-value scale))))

(defn numeric-relations
  "Construct numeric relations for the actual value to the numbers on a scale
    * actual-value - a number which has to be expressed
    * scale - specifies the granularity of the rounding:
              1/10 for one decimal point, 10 for rounding to tenths, and so on."
  [actual-value scale]
  (let [value-range                   (no/bounding-box actual-value scale)
        [[num> delta>] [num< delta<]] (distances-from-edges actual-value value-range)
        closest-num                   (min num> num<)
        equal-to                      (cond (= delta> 0.0) num>
                                            (= delta< 0.0) num<
                                            :else          nil)]
    (cond
      equal-to                         {:numwords/equal equal-to}
      (unreliable? actual-value scale) {:numwords/around closest-num}
      :else                            {:numwords/around closest-num
                                        :numwords/more   num>
                                        :numwords/less   num<})))

(s/fdef numeric-relations
  :args (s/cat :actual-value :numwords/actual-value
               :scale        :numwords/scale)
  :ret :numwords/given-value-relations)

(defn number->text
  "Translate number to text in a given language"
  [language number] (text/number->text language number))

(def config (cfg/numwords-config))

(defn hedge
  "List of words describing the relation between given and actual value"
  [language relation]
  (let [relation-kw (keyword (name relation))] ;FIXME how to do simple keyword given namespaced one?
    (-> config language :hedges relation-kw)))

(defn favorite-number
  "List of phrases which can be used instead of the number. Like `a half`"
  [language value] (-> config language :favorite-numbers (get value)))

(defn exact? [relations] (= '(:numwords/equal) (keys relations)))

(defn approximate? [relations] (= '(:numwords/around) (keys relations)))

(defn number-expression [language actual-value scale formatting relation]
  (let [relations (numeric-relations actual-value scale)]
    (cond
      (exact? relations)  "exact"
      (approximate? relations) "aprox"
      :else                                          "full"
      )))

