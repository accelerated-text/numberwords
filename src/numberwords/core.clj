(ns numberwords.core
  (:require [clojure.spec.alpha :as s]
            [numberwords.approx-math :as math]
            [numberwords.config :as cfg]
            [numberwords.formatting.bitesize :as bitesize]
            [numberwords.formatting.text :as text]
            [numberwords.domain :as nd]))

(def config (cfg/numwords-config))

(defn numeric-relations
  "Construct numeric relations for the actual value to the numbers on a scale
    * actual-value - a number which has to be expressed
    * scale - specifies the granularity of the rounding:
              1/10 for one decimal point, 10 for rounding to tenths, and so on."
  [actual-value scale]
  (let [[[num> delta>] [num< delta<]] (math/distances-from-edges actual-value scale)
        closest-num                   (min num> num<)
        equal-to                      (cond (= delta> 0.0) num>
                                            (= delta< 0.0) num<
                                            :else          nil)]
    (cond
      equal-to                              {::nd/equal equal-to}
      (math/unreliable? actual-value scale) {::nd/around closest-num}
      :else                                 {::nd/around closest-num
                                             ::nd/more   num>
                                             ::nd/less   num<})))

(s/fdef numeric-relations
  :args (s/cat :actual-value ::nd/actual-value
               :scale        ::nd/scale)
  :ret ::nd/given-value-relations)

(defn number->text
  "Translate number to text in a given language"
  [language number] (text/number->text language number))

(s/fdef number->text
  :args (s/cat :lang ::nd/language :num ::nd/actual-value)
  :ret string?)

(defn number->bitesize
  "Translate number to text in a given language"
  [number] (bitesize/number->bitesize number))

(s/fdef number->bitesize
  :args (s/cat :num ::nd/actual-value)
  :ret string?)

(defn hedge
  "List of words describing the relation between given and actual value"
  [language relation]
  ;;FIXME how to do simple keyword given namespaced one?
  (let [relation-kw (keyword (name relation))]
    (-> config language :hedges relation-kw)))

(s/fdef hedge
  :args (s/cat :lang ::nd/language :relation ::nd/relation)
  :ret (s/coll-of string? :kind set?))

(defn favorite-number
  "List of phrases which can be used instead of the number. Like `a half`"
  [language value] (-> config language :favorite-numbers (get value)))

(s/fdef favorite-number
  :args (s/cat :lang ::nd/language :relation ::nd/given-value)
  :ret (s/or :has-fav-nums (s/coll-of string? :kind set?)
             :no-fav-nums nil?))

(defn number-with-precision [num scale]
  (if (ratio? num)
    (double num)
    num))

(s/def ::nd/formatting #{::nd/words ::nd/bites ::nd/numbers})

(defn numeric-expression [language actual-value scale relation formatting]
  (let [relations   (numeric-relations actual-value scale)
        given-value (get relations relation
                         (get relations ::nd/equal
                              (get relations ::nd/around)))
        fav-num     (first (favorite-number language given-value))]
    (format "%s %s"
            (first (hedge language relation))
            (condp = formatting
              ::nd/numbers (number-with-precision given-value scale)
              ::nd/words   (or fav-num (number->text language given-value))
              ::nd/bites   (number->bitesize given-value)
              ))))
