(ns tentacles.core-test
  (:use clojure.test)
  (:require [tentacles.core :as core]))

(deftest hitting-rate-limit-is-propagated
  (is (= (:status (core/safe-parse {:status 403}))
         403)))

(deftest rate-limit-details-are-propagated
  (is (= 60 (:call-limit (core/api-meta
                          (core/safe-parse {:status 200 :headers {"x-ratelimit-limit" "60"
                                                                  "content-type" ""}}))))))

(deftest poll-limit-details-are-propagated
  (is (= 61 (:poll-interval (core/api-meta
                             (core/safe-parse {:status 200
                                               :headers {"x-poll-interval" "61"
                                                         "content-type" ""}}))))))
