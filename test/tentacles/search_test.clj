(ns tentacles.search-test
  (:use clojure.test)
  (:use tentacles.search))

(deftest query-should-not-contains-text-keyword
  (is (= "foo bar language:clojure language:scala"
         (search-term ["foo" "bar"] {:language ["clojure" "scala"]}))))

(deftest qualifier-with-one-element-seq-should-equals-qualifier-with-string-element
  (is (= (search-term ["foo"] {:language ["clojure"]})
         (search-term "foo" {:language "clojure"}))))

(deftest query-should-not-contains-nil-qualifier
  (is (= "foo"
         (search-term "foo" {:language nil}))))