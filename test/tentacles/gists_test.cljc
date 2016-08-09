(ns tentacles.gists-test
  (:require #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is testing run-tests]])
               [tentacles.gists :as gists]))

(def gist {:files {:file1 {:filename "file1" :content "content1" :type "text/plain"} :file2 {:filename "file2" :content "content2"}}})

(deftest files-are-parsed
  (let [files (gists/file-contents gist)]
    (is (= (count files) 2))
    (is (= (:file1 files) "content1"))))
