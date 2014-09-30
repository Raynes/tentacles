(ns tentacles.search-syntax-test
  (:use clojure.test)
  (:use tentacles.search-syntax))

(deftest query-should-not-contains-text-keyword
  (is (= "foo+bar+language:clojure+language:scala"
         (generate-query-string {:language ["clojure" "scala"] :text ["foo" "bar"]}))))

(deftest do-not-associate-nil-value
  (let [init {:text :foo}]
    (is (= {:text :foo :added :bar}
           (-> init
               (assoc-not-nil-value :added :bar)
               (assoc-not-nil-value :skipped nil))))))