(ns tentacles.search-syntax-test
  (:use clojure.test)
  (:use tentacles.search-syntax))

(deftest query-should-not-contains-text-keyword
  (is (= "foo bar language:clojure language:scala"
         (query-str ["foo" "bar"] {:language ["clojure" "scala"]}))))

(deftest criteria-with-one-element-seq-should-equals-criteria-with-string-element
  (is (= (query-str ["foo"] {:language ["clojure"]})
         (query-str "foo" {:language "clojure"}))))

(deftest query-should-not-contains-nil-criteria
  (is (= "foo"
         (query-str "foo" {:language nil}))))