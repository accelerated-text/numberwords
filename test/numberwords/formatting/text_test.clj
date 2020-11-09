(ns numberwords.formatting.text-test
  (:require [numberwords.formatting.text :refer [number->text]]
            [clojure.test :refer [deftest is]]))

(deftest number-conversion
  (is (= "ten" (number->text 10)))
  (is (= "zehn" (number->text :de 10))))
