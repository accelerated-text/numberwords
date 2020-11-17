(ns numberwords.formatting.bitesize
  (:require [clojure.spec.alpha :as s]))

(def sizes '("" "k" "M" "B" "T"))

(s/def ::letter (set sizes))
(s/def ::number number?)

(defn invalid? [n] (or (Double/isNaN n) (Double/isInfinite n)))

(defn number->bitesize
  "Algorithm taken from:
  https://programming.guide/java/formatting-byte-size-to-human-readable-format.html "
  [n]
  (if (invalid? n)
    {::number n}
    (loop [number         (Math/abs n)
           number-letters sizes]
      (cond
        (nil? (peek number-letters)) (str n)
        (= 0 (quot number 1000))     (str number (peek number-letters))
        :else                        (recur (quot number 1000) (pop number-letters))))))

(s/fdef number->bitesize
  :args (s/cat :number number?)
  :ret  string?)
