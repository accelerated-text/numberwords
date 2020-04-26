lint:
	clojure -Sdeps '{:deps {clj-kondo {:mvn/version "RELEASE"}}}' -m clj-kondo.main --lint src test

.PHONY: test
test:
	clojure -A:test:runner

