(ns numberwords.formatting.bitesize
  (:require [clojure.spec.alpha :as s]))

(s/def ::letter #{"" "k" "M" "B" "T"})
(s/def ::number number?)
(s/def ::bite-expression (s/keys :req [::letter ::number]))

(defn invalid? [n] (or (Double/isNaN n) (Double/isInfinite n)))

(defn bite-count-formatting
  "Algorithm taken from:
  https://programming.guide/java/formatting-byte-size-to-human-readable-format.html "
  [n]
  (if (invalid? n)
    {::number n}
    (loop [number         (Math/abs n)
           number-letters '("" "k" "M" "B" "T")]
      (cond
        (nil? (peek number-letters)) {::number n}
        (= 0 (quot number 1000))     {::letter (peek number-letters)
                                      ::number number}
        :else                        (recur (quot number 1000) (pop number-letters))))))

(s/fdef bite-count-formatting
  :args (s/cat :number number?)
  :ret  (s/or :sucess  ::bite-expression
              :too-big (s/keys :req [::number])))
