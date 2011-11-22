(ns tentacles.test
  (:require [tentacles.repos :as repo]))

(def resource
  (repo/download-resource "dakrone"
                          "clj-http"
                          "foo2.txt"
                          {:auth "dakrone:yourpasshere"}))

;; (upload-file resource)
;; This will fail. You will not be able to create a new resource
;; every time, so if you try to upload more than once, use the same
;; resource each time. If you need a new resource, use repo/delete-download.
;; The download id is in the resource map.