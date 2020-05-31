lint:
	clojure -Sdeps '{:deps {clj-kondo {:mvn/version "RELEASE"}}}' -m clj-kondo.main --lint src test --config '{:output {:exclude-files ["java"]}}'

.PHONY: test
test:
	clojure -A:test:runner

uberjar:
	clj -A:uberjar

