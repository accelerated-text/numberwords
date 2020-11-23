(ns numberwords.java
  (:require [clojure.pprint :as pp]
            [numberwords.core :as nw]
            [numberwords.domain :as nd]
            [clojure.string :as string]))

(gen-class :name ai.tokenmill.numberwords.NumberWords
           :main true
           :prefix NW-
           :methods [[numericExpression
                      [Double Double String String String]
                      java.lang.String]])

(def label->relation {"equal"  ::nd/equal
                      "around" ::nd/around
                      "less"   ::nd/less
                      "more"   ::nd/more})

(def label->format {"words"   ::nd/words
                    "bites"   ::nd/bites
                    "numbers" ::nd/numbers})

(defn NW-numericExpression [_ actual-value scale language
                             relation formatting]
  (nw/numeric-expression actual-value scale (keyword language)
                         (get label->relation relation)
                         (get label->format formatting)))

(defn NW-main [& [actual-value scale language relation formatting]]
  (pp/pprint
   (NW-numericExpression
    nil
    (if (string/includes? actual-value ".")
      (Double/parseDouble actual-value)
      (Integer/parseInt actual-value))
    (if (string/includes? scale ".")
      (Double/parseDouble scale)
      (Integer/parseInt scale))
    language
    relation
    formatting)))
