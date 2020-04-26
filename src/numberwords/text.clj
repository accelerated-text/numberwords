(ns numberwords.text
  (:import com.ibm.icu.text.RuleBasedNumberFormat
           java.util.Locale))

(def spellout
  {:en (RuleBasedNumberFormat. RuleBasedNumberFormat/SPELLOUT)
   :de (RuleBasedNumberFormat. Locale/GERMAN
                               RuleBasedNumberFormat/SPELLOUT)})

(defn number->text
  ([language number] (.format (get spellout language) number))
  ([number] (number->text :en number)))
