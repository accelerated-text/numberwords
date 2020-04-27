[![Clojars Project](https://img.shields.io/clojars/v/numberwords.svg)](https://clojars.org/numberwords)

# Number Words

Number Words will build numeric expressions for natural numbers, percentages and fractions. For example:

* `0.231` will be converted to `less than a quarter`,
* `102` to `over one hundred`.

Supports multiple languages.

The implementation is based on ideas expressed in [Generating Numerical Approximations](https://www.mitpressjournals.org/doi/full/10.1162/COLI_a_00086).

## Numerical Approximations


Numerical approximations are all over texts based on the data:

```
- Water temperature is below 10C (input data would be 9.53C)
- A third of students failed the exam (34.3%)
- Q2 sales were over 1M$ (1,002,184 $)
```

Numeric data providing information about some metrics of interest is often a number with the precision we do not need. If we see 9.382%, it is likely that the information we need is - *almost 10%* - instead of the precise number. Furthermore, different approximation strategies are often used in the report involving the same metrics. At the beginning of the report we might say *almost 10%* or "below 10%" while later in the text, we might choose a more precise expression - *around 9.4%*.

*Number Words* will help you build such numerical approximations. Making them available for the text generation systems.

## Features

Number Words uses the following abstractions:
* *Actual Value* is a number which needs to be approximated - an input to the approximation function. In the examples above it is the temperature - `9.53C`, or the percentage `34.3%`.
* *Scale* of approximation. It is a snapping grid across the range of numbers along which the approximation is done. The scale to use is determined by the domain. For example:
  * `1/4` scale, will form approximation steps starting at `0` then `1/4`, `1/2`, `3/4` ending with `1`;
  * `1/10` scale will express percentages with one precision point;
  * scales which are multiples of `10` are useful for natural number approximation. The `10` will round to tens: `1007` -> `1010`, the `100` to hundreds: `1003` -> `1000`, and so on.
  
The result of *actual value* approximation to a given scale provides:
* *Given Value* a discrete value along the scaled number range to which *actual value* is the closest.
* *Hedge* a common use word describing the relation between *actual* and *given* values. 
  *Actual Value* of `9.5` is **below** *given value* of `10`.
  *Actual Value* of `101` is **over** *given value* of `100`.
* *Text* a textual spell out of the *given value*. A `2666` is `Two thousand six hundred sixty six`.
* *Favorite Number* expresses some common language names for certain numbers. A `0.25` is a favorite number in that that it has the name - `a quarter`.

A full approximation result returns three such approximation data structures for a *given value* which is:
* **smaller** than the *actual value* on the scaled number range. 
* **greater** than the *actual value* on the scaled number range. 
* **around** the *actual value* on the scaled number range. For this a is chosen from the above two which is closer to the *actual value*.
  
## Languages

Numeric approximation has two functionality points which are language dependent
* *Hedges* which will differ from language to language. See [Configuration]() section to see how this can be controlled.
* *Text* number to text translation for a *given value*. For this translation Number Words relies on [ICU4J](https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/).

Currently supported languages:
* English
* German
* Portuguese
  
## Usage

Number Words exposes approximation functionality through `approximations` function which takes on the following parameters:
* `language` - `:de` or `:en`
* `actual-value` - the number to approximate
* `scale` - at which the approximation is to be performed.

```
(require '[numberwords.core :as nw])

(nw/approximations :en 0.258 1/4)
=>
#:numwords{:around
           #:numwords{:hedges #{"approximately" "about" "around"},
                      :text "zero point two five",
                      :given-value 1/4,
                      :favorite-number #{"a quarter"}},
           :more-than
           #:numwords{:hedges #{"over" "more than"},
                      :text "zero point two five",
                      :given-value 1/4,
                      :favorite-number #{"a quarter"}},
           :less-than
           #:numwords{:hedges #{"nearly" "under" "less than"},
                      :text "zero point five",
                      :given-value 1/2,
                      :favorite-number #{"a half"}}}
```

## Configuration

Hedges, favorite numbers can be modified and new languages added via changes to a configuration file - `resources/numwords.edn`

```
{;;Configuration is strucutured by the language 
 :en {
      ;;Hedges section specifies which words are associated with given actual to given value relations
      :hedges {:equal  #{"exactly"}
               :around #{"around" "approximately" "about"}
               :more   #{"more than" "over"}
               :less   #{"less than" "under" "nearly"}}
      
      ;;Favourite numbers map a special number with its textual expressions
      :favorite-numbers {1/4  #{"a quarter" "a fourth"}
                         1/2  #{"a half"}}}}
```

## License

Copyright &copy; 2020 [TokenMill UAB](http://www.tokenmill.ai).

Distributed under the The Apache License, Version 2.0.
