(ns tentacles.core-test
  (:use clojure.test)
  (:require [tentacles.core :as core]))

(deftest request-contains-user-agent
  (let [request (core/make-request :get "test" nil {:user-agent "Mozilla"})]
    (do (is (empty?    (:query-params request)))
      (is (contains? (:headers request) "User-Agent"))
      (is (= (get (:headers request) "User-Agent") "Mozilla")))))

(deftest settable-enterprise-end-point-to-request
  (let [end-point "https://hostname/api/v3/"]
    (testing "When default url"
      (let [request (core/make-request :get "test" nil nil)]
        (is (= (:url request) "https://api.github.com/test"))))
    (testing "When set url with option"
      (let [request (core/make-request :get "test" nil {:url end-point})]
        (is (= (:url request) (str end-point "test")))))
    (testing "When set url with binding macro"
      (binding [core/url end-point]
        (let [request (core/make-request :get "test" nil nil)]
          (is (= (:url request) (str end-point "test"))))))))

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
