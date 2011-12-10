(ns tentacles.core-test
  (:use midje.sweet
        tentacles.core))

(fact
 (query-map {:foo 0
             :foo-bar 0
             "baz-quux" 0}) => {"foo" 0 "foo_bar" 0 "baz_quux" 0})

(fact
 (parse-json nil) => nil?
 (parse-json "{\"foo\" : 0}") => {:foo 0})

(letfn [(status-map [status] {:status status :body 0})]
  (tabular
   (fact
    (safe-parse {:status ?status :body "0"}) => ?expected
    (provided))
   ?status ?expected
   404     (status-map 404)
   400     (status-map 400)
   422     (status-map 422)
   500     (status-map 500)
   204     (status-map 204)
   200     0))

(fact
 (no-content? {:status 204}) => truthy
 (no-content? {:status 200}) => falsey)