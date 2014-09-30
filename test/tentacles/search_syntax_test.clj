(ns tentacles.search-syntax-test
  (:use clojure.test)
  (:use tentacles.search-syntax))

(deftest query-should-not-contains-text-keyword
  (is (= "foo+bar+language:clojure+language:scala"
         (query-str {:language ["clojure" "scala"] :text ["foo" "bar"]}))))

(deftest criteria-with-one-element-seq-should-equals-criteria-with-string-element
  (is (= (query-str {:language ["clojure"] :text ["foo"]})
         (query-str {:language "clojure" :text "foo"}))))

(deftest query-should-not-contains-nil-criteria
  (is (= "foo"
         (query-str {:language nil :text "foo"}))))