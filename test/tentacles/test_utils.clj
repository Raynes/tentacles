(ns tentacles.test-utils)

(def test-info (read-string (slurp "testinfo.clj")))
(def auth {:auth (str (:user test-info) ":" (:pass test-info))})