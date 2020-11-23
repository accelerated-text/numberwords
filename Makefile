lint:
	clojure -Sdeps '{:deps {clj-kondo {:mvn/version "RELEASE"}}}' -m clj-kondo.main --lint src test --config '{:output {:exclude-files ["java"]}}'

.PHONY: test
test:
	clojure -M:test:runner

.PHONY: uberjar
uberjar:
	clj -M:uberjar

.PHONY: deploy
deploy:
	clj -M:deploy
