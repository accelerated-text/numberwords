(ns numberwords.java
  (:require [numberwords.core :as nw]))

(gen-class :name ai.tokenmill.numberwords.NumberWords
           :methods [[approximations [String Float Float] java.util.Map]])

(defn -approximations [_ language actual-value scale]
  (prn "Lang: " (keyword language))
  (prn "AV: " actual-value)
  (prn "SC: " (rationalize scale))
  (nw/approximations (keyword language) actual-value (rationalize scale)))
