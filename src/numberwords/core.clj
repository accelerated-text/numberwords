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
  [number language] (text/number->text language number))

(s/fdef number->text
  :args (s/cat :num ::nd/actual-value :lang ::nd/language)
  :ret string?)

(defn number->bitesize
  "Translate number to bite style number formatting"
  [number] (bitesize/number->bitesize number))

(s/fdef number->bitesize
  :args (s/cat :num ::nd/actual-value)
  :ret string?)

(defn hedge
  "List of words describing the relation between given and actual value"
  [relation language]
  ;;FIXME how to do simple keyword given namespaced one?
  (let [relation-kw (keyword (name relation))]
    (-> config language :hedges relation-kw)))

(s/fdef hedge
  :args (s/cat :relation ::nd/relation :lang ::nd/language)
  :ret (s/coll-of string? :kind set?))

(defn favorite-number
  "List of phrases which can be used instead of the number. Like `a half`"
  [value language] (-> config language :favorite-numbers (get value)))

(s/fdef favorite-number
  :args (s/cat :relation ::nd/given-value :lang ::nd/language)
  :ret (s/or :has-fav-nums (s/coll-of string? :kind set?)
             :no-fav-nums nil?))

(defn number-with-precision [num scale]
  (if (ratio? num)
    (double num)
    num))

(defn possible-relation
  "Get the relation which is possible in the current given value approximations.
  If we have regular case with all three (less,more,equal) detected then return it
  else first check if we have 'equal' relation, if this is not present go for 'around'"
  [given-val-relations requested-relation]
  (let [relation-types (set (keys given-val-relations))]
    (or
     (get relation-types requested-relation)
     (get relation-types ::nd/equal)
     (get relation-types ::nd/about))))

(defn numeric-expression
  ([actual-value scale]
   (numeric-expression actual-value scale :en ::nd/around ::nd/bites))
  ([actual-value scale relation formatting]
   (numeric-expression actual-value scale :en relation formatting))
  ([actual-value scale language relation formatting]
   (let [relations       (numeric-relations actual-value scale)
         actual-relation (possible-relation relations relation)
         given-value     (get relations actual-relation)
         fav-num         (first (favorite-number given-value language))]
     (format "%s %s"
             (first (hedge actual-relation language))
             (condp = formatting
               ::nd/numbers (number-with-precision given-value scale)
               ::nd/words   (or fav-num (number->text given-value language))
               ::nd/bites
               ;;FIXME this part is not good, plus revisit
               ;; `number-with-precision` it has to work for `numbers`
               (if (or (rational? given-value)
                                    (ratio? given-value)
                                    (< scale 1))
                              (number-with-precision given-value scale)
                              (number->bitesize given-value)))))))

(s/fdef numeric-expression
  :args (s/cat :num ::nd/actual-value :scale ::nd/scale :lang ::nd/language
               :relation ::nd/relation :formatting ::nd/formatting)
  :ret string?)
