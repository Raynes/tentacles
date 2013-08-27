(ns tentacles.oauth
  (:use [tentacles.core :only [api-call no-content?]]))

(defn authorizations
  "List your authorizations."
  [options]
  (api-call :get "authorizations" nil options))

(defn specific-auth
  "Get a specific authorization."
  [id & [options]]
  (api-call :get "authorizations/%s" [id] options))

(defn delete-auth
  "Delete an authorization."
  [id & [options]]
  (no-content? (api-call :delete "authorizations/%s" [id] options)))

(defn create-auth
  "Create a new authorization."
  [options]
  (api-call :post "authorizations" nil options))

(defn valid-auth?
  "Returns auth data if authorization is still valid, false otherwise.
  OAuth applications can use this special API method for checking
  OAuth token validity without running afoul of normal rate limits for
  failed login attempts. Authentication works differently with this
  particular endpoint. You must use Basic Authentication when
  accessing it, where the username is the OAuth application client_id
  and the password is its client_secret."
  [client-id access-token & [options]]
  (let [result (api-call :get "applications/%s/tokens/%s" [client-id access-token] options)]
    (if (= (:status result) 404)
      false
      result)))
