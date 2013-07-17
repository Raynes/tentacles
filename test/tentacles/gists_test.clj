(ns tentacles.gists-test
  (:use clojure.test)
  (:require [tentacles.gists :as gists]))

(def gist {:files {:file1 {:filename "file1" :content "content1" :type "text/plain"} :file2 {:filename "file2" :content "content2"}}})

(deftest files-are-parsed
  (let [files (gists/file-contents gist)]
    (is (= (count files) 2))
    (is (= (:file1 files) "content1"))))
