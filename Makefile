lint:
	clojure -Sdeps '{:deps {clj-kondo {:mvn/version "RELEASE"}}}' -m clj-kondo.main --lint src test

.PHONY: test
test:
	clojure -A:test:runner

recompile-java-interface:
	rm -rf classes
	mkdir classes
	clojure -e "(require 'numberwords.java) (compile 'numberwords.java)"


