;; The naming in this particular API implementation might be slightly different
;; compared to the rest of the API implementations. Since you can only edit one
;; user -- yourself -- it doesn't make sense to name it edit-user. Things like
;; that.
(ns tentacles.users
  "Implement the Github Users API: http://developer.github.com/v3/users/"
  (:use [tentacles.core :only [api-call]]
        [slingshot.slingshot :only [try+]]))

;; ## Primary API

(defn user
  "Get info about a user."
  [user]
  (api-call :get "users/%s" [user] nil))

(defn me
  "Get info about the currently authenticated user."
  [options]
  (api-call :get "user" nil options))

(defn edit
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
  (nil? (api-call :delete "user/emails" nil (assoc options :raw emails))))