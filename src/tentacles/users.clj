(ns tentacles.users
  "Implement the Github Users API: http://developer.github.com/v3/users/"
  (:refer-clojure :exclude [keys])
  (:use [tentacles.core :only [api-call no-content?]]))

;; ## Primary API

(defn user
  "Get info about a user."
  [user & [options]]
  (api-call :get "users/%s" [user] nil options))

(defn me
  "Get info about the currently authenticated user."
  [options]
  (api-call :get "user" nil options))

(defn edit-user
  "Edit the currently authenticated user.
   Options are:
      name     -- User's name.
      email    -- User's email.
      blog     -- Link to user's blog.
      location -- User's location.
      hireable -- Looking for a job?
      bio      -- User's biography."
  [options]
  (api-call :post "user" nil options))

;; User Email API

(defn emails
  "List the authenticated user's emails."
  [options]
  (api-call :get "user/emails" nil options))

(defn add-emails
  "Add email address(es) to the authenticated user. emails is either
   a string or a sequence of emails addresses."
  [emails options]
  (api-call :post "user/emails" nil (assoc options :raw emails)))

(defn delete-emails
  "Delete email address(es) from the authenticated user. Emails is either
   a string or a sequence of email addresses."
  [emails options]
  (no-content? (api-call :delete "user/emails" nil (assoc options :raw emails))))

;; User Followers API

(defn followers
  "List a user's followers."
  [user & [options]]
  (api-call :get "users/%s/followers" [user] options))

(defn my-followers
  "List the authenticated user's followers."
  [options]
  (api-call :get "user/followers" nil options))

(defn following
  "List the users a user is following."
  [user & [options]]
  (api-call :get "users/%s/following" [user] options))

(defn my-following
  "List the users the authenticated user is following."
  [options]
  (api-call :get "user/following" nil options))

(defn following?
  "Check if the authenticated user is following another user."
  [user options]
  (no-content? (api-call :get "user/following/%s" [user] options)))

(defn follow
  "Follow a user."
  [user options]
  (no-content? (api-call :put "user/following/%s" [user] options)))

(defn unfollow
  "Unfollow a user."
  [user options]
  (no-content? (api-call :delete "user/following/%s" [user] options)))

;; User Keys API

(defn keys
  "List the authenticated user's public keys."
  [options]
  (api-call :get "user/keys" nil options))

(defn specific-key
  "Get a specific key from the authenticated user."
  [id options]
  (api-call :get "user/keys/%s" [id] options))

(defn create-key
  "Create a new public key."
  [title key options]
  (api-call :post "user/keys" nil (assoc options :title title :key key)))

(defn edit-key
  "Edit an existing public key.
   Options are:
      title -- New title.
      key   -- New key."
  [id options]
  (api-call :post "user/keys/%s" [id] options))

(defn delete-key
  "Delete a public key."
  [id options]
  (no-content? (api-call :delete "user/keys/%s" [id] options)))