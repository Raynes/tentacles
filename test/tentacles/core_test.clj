(ns tentacles.core-test
  (:use clojure.test)
  (:require [tentacles.core :as core]))

(deftest hitting-rate-limit-is-propagated
   (is (= (:status (core/safe-parse {:status 403}))
     403)))

(deftest rate-limit-details-are-propagated-when-defined
   (is (contains? (core/safe-parse {:status 200 :X-RateLimit-Limit 20 :headers {"content-type" ""}}) :X-RateLimit-Limit)))

(deftest rate-limit-details-are-ignored-when-undefined
   (is (not (contains? (core/safe-parse {:status 200 :headers {"content-type" ""}}) :X-RateLimit-Limit))))
