(ns tentacles.events
  "Implements the Github Events API: http://developer.github.com/v3/events/"
  (:use [tentacles.core :only [api-call]]))

(defn events
  "List public events."
  []
  (api-call :get "events" nil nil))

(defn repo-events
  "List repository events."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/events" [user repo] options))

(defn issue-events
  "List issue events for a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/issues/events" [user repo] options))

(defn network-events
  "List events for a network of repositories."
  [user repo & [options]]
  (api-call :get "networks/%s/%s/events" [user repo] options))

(defn user-events
  "List events that a user has received. If authenticated, you'll see
   private events, otherwise only public."
  [user & [options]]
  (api-call :get "users/%s/received_events" [user] options))

(defn performed-events
  "List events perofmred by a user. If you're authenticated, you'll see
   private events, otherwise you'll only see public events."
  [user & [options]]
  (api-call :get "users/%s/events" [user] options))

;; Even though this requires authentication, you still need to pass the
;; username in the URL. I can work around this, but I don't feel like it
;; right now.
(defn org-events
  "List an organization's events."
  [user org options]
  (api-call :get "users/%s/events/orgs/%s" [user org] options))