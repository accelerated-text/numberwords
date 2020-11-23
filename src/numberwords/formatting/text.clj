(ns numberwords.formatting.text
  (:import com.ibm.icu.text.RuleBasedNumberFormat
           java.util.Locale))

(def spellout
  {:en (RuleBasedNumberFormat. RuleBasedNumberFormat/SPELLOUT)
   :de (RuleBasedNumberFormat. Locale/GERMAN
                               RuleBasedNumberFormat/SPELLOUT)})

(defn num-formater-for [language]
  (RuleBasedNumberFormat. (Locale/forLanguageTag (name language))
                          RuleBasedNumberFormat/SPELLOUT))

(defn number->text
  ([language number] (.format (num-formater-for language) number))
  ([number] (number->text :en number)))
