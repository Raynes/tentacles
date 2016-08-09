(ns tentacles.search-test
  (:require #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is testing run-tests]])
               [tentacles.search :refer [search-term]]))

(deftest query-should-not-contains-text-keyword
  (is (= "foo bar language:clojure language:scala"
         (search-term ["foo" "bar"] {:language ["clojure" "scala"]}))))

(deftest qualifier-with-one-element-seq-should-equals-qualifier-with-string-element
  (is (= (search-term ["foo"] {:language ["clojure"]})
         (search-term "foo" {:language "clojure"}))))

(deftest query-should-not-contains-nil-qualifier
  (is (= "foo"
         (search-term "foo" {:language nil}))))
