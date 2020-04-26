#!/bin/bash -e
cd "$( dirname "${BASH_SOURCE[0]}" )"
clojure -m uberdeps.uberjar --deps-file ../deps.edn --target ../target/numberwords-1.0.1.jar
