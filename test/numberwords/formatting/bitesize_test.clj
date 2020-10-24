(ns numberwords.formatting.bitesize-test
  (:require [numberwords.formatting.bitesize :as bs]
            [clojure.test :refer [deftest is]]))

(deftest bitesize-formatting
  (is (= {::bs/letter "" ::bs/number 1} (bs/bite-count-formatting 1)))
  (is (= {::bs/letter "k" ::bs/number 1} (bs/bite-count-formatting 1999)))
  (is (= {::bs/number 100000000000000000} (bs/bite-count-formatting 100000000000000000))))

