(ns tentacles.oauth
  (:use [tentacles.core :only [api-call no-content?]]))

(defn authorizations [options]
  (api-call :get "authorizations" nil options))

(defn specific-auth [id & [options]]
  (api-call :get "authorizations/%s" [id] options))

(defn delete-auth [id & [options]]
  (no-content? (api-call :delete "authorizations/%s" [id] options)))