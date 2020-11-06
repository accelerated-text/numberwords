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

;;numeric approximation provides all the info describing how an actual
;;value can be described in an approximate manner
(s/def :numwords/num-approximation (s/keys :req [:numwords/text :numwords/hedges :numwords/given-value]
                                           :upt [:numwords/favorite-number]))

;;main resulting data structure with three branches
;; :equal in case actual value is equal to given value
;; :unreliable will be provided when working with huge numbers and where scale is
;;             larger than actual value
;; :unequal main case when we will have all three numeric expressions generated
(s/def :numwords/numeric-expressions
  (s/or :equal      (s/map-of #{:numwords/equal} :numwords/num-approximation)
        :unreliable (s/map-of #{:numwords/around} :numwords/num-approximation)
        :unequal    (s/map-of #{:numwords/around :numwords/more :numwords/less}
                              :numwords/num-approximation :min-count 3)))

(s/def :numwords/relation #{:numwords/around :numwords/more :numwords/lest :equal})

(s/def :numwords/approximations (s/keys :req [:numwords/given-value :numwords/relation]))

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

(defn build-expr
  "Build resulting spec conformant numeric expression description"
  [hedges fav-numbers text given-value]
  (cond-> {:numwords/hedges      hedges
           :numwords/text        text
           :numwords/given-value given-value}
    fav-numbers (assoc :numwords/favorite-number fav-numbers)))

(defn unreliable?
  "Actual value and scale values which will generate unreliable results:
  - actual-value much bigger that the scale
  - scale is bigger than the actual value"
  [actual-value scale]
  ;; 1000x difference between the scale and value is a random choice
  (or (< 1000 (/ actual-value scale))
      (and (< 1 scale) (< actual-value scale))))

(defn approximations
  "Numeric approximations translate given numeric value to a set of simplified
  approximations of that number. Function parameters:

    * actual-value - a number which has to be expressed
    * language - to be used for text generation
    * scale - specifies the granularity of the rounding:
              1/10 for one decimal point, 10 for rounding to tenths, and so on.

  Resulting approximation provides:

    * given-value - a number which is a closest approximation of the actual value
    * relation - how given-value relates to actual value: equal, more, less, or around
    * hedges - a list of words describing the relation
    * text - given-number translated to text"
  [language actual-value scale]
  (let [{:keys [hedges favorite-numbers]}
        (cfg/numwords-for language)
        text                          (partial text/number->text language)
        value-range                   (no/bounding-box actual-value scale)
        [[num> delta>] [num< delta<]] (distances-from-edges actual-value value-range)
        closest-num                   (min num> num<)
        equal-to                      (cond (= delta> 0.0) num>
                                            (= delta< 0.0) num<
                                            :else          nil)]
    (if equal-to
      {:numwords/equal (build-expr (hedges :equal)
                                   (favorite-numbers equal-to)
                                   (text equal-to)
                                   equal-to)}
      (let [around (build-expr (hedges :around)
                               (favorite-numbers closest-num)
                               (text closest-num)
                               closest-num)]
        (if (unreliable? actual-value scale)
          {:numwords/around around}
          {:numwords/around    around
           :numwords/more (build-expr (hedges :more)
                                           (favorite-numbers num>)
                                           (text num>)
                                           num>)
           :numwords/less (build-expr (hedges :less)
                                           (favorite-numbers num<)
                                           (text num<)
                                           num<)})))))

(defn approximations2 [actual-value scale]
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

(defn number->text [language number] (text/number->text language number))

(def config (cfg/numwords-config))

(defn hedge [language relation]
  (let [relation-kw (keyword (name relation))] ;FIXME how to do simple keyword given namespaced one?
    (-> config language :hedges relation-kw)))

(defn favorite-number [language value] (-> config language :favorite-numbers (get value)))

(defn number-expression [language actual-value scale formatting relation]
  (let [numexp-variants (approximations2 actual-value scale)]
    (cond
      (= '(:numwords/equal) (keys numexp-variants))  "exact"
      (= '(:numwords/around) (keys numexp-variants)) "aprox"
      :else                                          "full"
      )))

(s/fdef approximations
  :args (s/cat :language     :numwords/language
               :actual-value :numwords/actual-value
               :scale        :numwords/scale)
  :ret :numwords/numeric-expressions)
