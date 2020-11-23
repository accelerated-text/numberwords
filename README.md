[![Clojars Project](https://img.shields.io/clojars/v/ai.tokenmill.numberwords/numberwords.svg)](https://clojars.org/ai.tokenmill.numberwords/numberwords)

# Number Words

![ZEN](docs/amanda-jones-Mk3rIB1JyTY-unsplash-cropped.png)

Number Words will build numeric expressions for natural numbers, percentages and fractions. For example:

* `0.231` will be converted to `less than a quarter`,
* `102` to `over one hundred`.

Supports multiple languages.

The implementation is based on ideas expressed in [Generating Numerical Approximations](https://www.mitpressjournals.org/doi/full/10.1162/COLI_a_00086).

## Numerical Approximations

Numerical approximations are all over texts based on data:

```
- Water temperature is below 10C (input data would be 9.53C)
- A third of students failed the exam (34.3%)
- Q2 sales were over 1M$ (1,002,184 $)
```

Numeric data providing information about some metrics of interest is often a number with the precision we do not need. If we see 9.382%, the information we need is likely - *almost 10%* - instead of the precise number. Furthermore, different approximation strategies are often used in the report involving the same metrics. At the beginning of the report we might say *almost 10%* or "below 10%" while later in the text, we might choose a more precise expression - *around 9.4%*.

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
  *Actual Value* of `9.5` is **below** the *given value* of `10`.
  *Actual Value* of `101` is **over** the *given value* of `100`.
* *Text* a textual spell out of the *given value*. A `2666` is `Two thousand six hundred sixty six`.
* *Favorite Number* expresses some common language names for certain numbers. A `0.25` is a favorite number in that that it has the name - `a quarter`.

A full approximation result returns three such approximation data structures for a *given value* which is:
* **less** than the *actual value* on the scaled number range. 
* **more** than the *actual value* on the scaled number range. 
* **around** the *actual value* on the scaled number range. For this 'less' or 'more' value closer to the *actual value* is chosen.

Lastly the number formatting can be specified:
* **words** - spell out the number in words (110 -> hundred and ten).
* **bites** - spell out the number using bite size style shortening (1022 -> 1k).
* **numbers** - report number as is.

## Languages

Numeric approximation has two functionality points which are language dependent
* *Hedges* which will differ from language to language. See [Configuration]() section to see how this can be controlled.
* *Text* number to text translation for a *given value*. For this translation Number Words relies on [ICU4J](https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/).

Currently supported languages:
* French
* German
* English
* Lithuanian 
* Portuguese
* Russian
  
## Usage

Number Words exposes approximation functionality through `numeric-expression` function which takes on the following parameters:
* `actual-value` - the number to approximate
* `scale` - at which the approximation is to be performed.
* `language` - use two letter language code (like :pt), default is :en
* `relation` - what kind of relation to between actual and given value to use (valid values specified in `:numberwords.domain/relation`)
* `formatting` - which number formatting should be used (valid values specified in `:numberwords.domain/formatting`)

### Installation

`Number Words` is available as a Maven artifact from [Clojars](https://clojars.org/ai.tokenmill.numberwords/numberwords).

### Clojure

_Leiningen_
```
[ai.tokenmill.numberwords/numberwords "1.1.0"]
```

_deps.edn_
```
ai.tokenmill.numberwords/numberwords {:mvn/version "1.1.0"}
```

Usage example:

```
(require '[numberwords.core :as nw])

(numeric-expression 144 10 :en :numberwords.domain/around :numberwords.domain/words)
=>
"around one hundred forty"

(numeric-expression 144 10 :de :numberwords.domain/less :numberwords.domain/numbers)
=>
"weniger als 150"

;; with defaults
(numeric-expression 144 10)
=>
"around 140"

```

### Java

Get a _jar_ by building it with 

```
clojure -A:uberjar
```

Or as a _Maven_ dependency

```
<repository>
    <id>clojars.org</id>
    <url>http://clojars.org/repo</url>
</repository>
<dependency>
    <groupId>ai.tokenmill.numberwords</groupId>
    <artifactId>numberwords</artifactId>
    <version>1.1.0</version>
</dependency>
```

Usage example:

```
import ai.tokenmill.numberwords.NumberWords;

NumberWords nw = new NumberWords();
nw.numericExpression(1.22, 0.1, "en", "more", "numbers");
```

## Configuration

Hedges, favorite numbers can be modified and new languages added via changes to a configuration file - `resources/numwords.edn`

```
{;;Configuration is structured by the language 
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

----
<span>Photo by <a href="https://unsplash.com/@amandagraphc?utm_source=unsplash&amp;utm_medium=referral&amp;utm_content=creditCopyText">Amanda Jones</a> on <a href="https://unsplash.com/?utm_source=unsplash&amp;utm_medium=referral&amp;utm_content=creditCopyText">Unsplash</a></span>
