(ns numberwords.config
  (:import java.io.PushbackReader)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn numwords-for [language]
  (with-open [r (io/reader (io/resource "numwords.edn"))]
    (let [config (get (edn/read (PushbackReader. r)) language)]
      {:hedges           (fn [relation] (get-in config [:hedges relation]))
       :favorite-numbers (fn [given-value]
                           (get-in config [:favorite-numbers given-value]))})))

(defn supported-langauges []
  (with-open [r (io/reader (io/resource "numwords.edn"))]
    (-> (PushbackReader. r)
        (edn/read)
        (keys)
        (set))))
