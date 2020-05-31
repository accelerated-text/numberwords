(ns numberwords.java
  (:require [clojure.pprint :as pp]
            [numberwords.core :as nw]))

(gen-class :name ai.tokenmill.numberwords.NumberWords
           :main true
           :prefix NW-
           :methods [[approximations [String Double Double] java.util.Map]])

(defn NW-approximations [_ language actual-value scale]
  (nw/approximations (keyword language) actual-value (rationalize scale)))

(defn -main [& [language actual-value scale]]
  (pp/pprint
   (NW-approximations nil language
                      (Double/parseDouble actual-value)
                      (Double/parseDouble scale))))
