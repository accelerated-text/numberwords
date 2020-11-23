(ns numberwords.formatting.bitesize-test
  (:require [numberwords.formatting.bitesize :refer [number->bitesize]]
            [clojure.test :refer [deftest are]]))

(deftest bitesize-formatting
  (are [results value] (= results (number->bitesize value))
    "1" 1
    "1k" 1999
    "100000000000000000" 100000000000000000))
