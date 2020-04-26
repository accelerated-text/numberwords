(ns numberwords.text-test
  (:require [numberwords.text :refer [number->text]]
            [clojure.test :refer [deftest is]]))

(deftest number-conversion
  (is (= "ten" (number->text 10)))
  (is (= "zehn" (number->text :de 10))))
