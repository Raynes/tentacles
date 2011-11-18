(ns tentacles.issues
  "Implements the Github Issues API: http://developer.github.com/v3/issues/"
  (:use [tentacles.core :only [api-call]]
        [clojure.string :only [join]]))

;; Some API requests, namely GET ones, require that labels be passed as a
;; comma-delimited string of labels. The POST requests want it to be passed
;; as a list of strings. In order to be consistent, users will always pass
;; a list of labels. This joins the labels so that the string requirement
;; on GETs is transparent to the user.
(defn- join-labels [m]
  (if (:labels m)
    (update-in m [:labels] (partial join ","))
    m))

;; ## Primary Issue API

(defn issues
  "List issues for (authenticated) user.
   Options are:
     filter    -- assigned: assigned to you,
                  created: created by you,
                  mentioned: issues that mention you,
                  subscribed: issues that you're subscribed to.
     state     -- open (default), closed.
     labels    -- A string of comma-separated label names.
     sort      -- created (default), updated, comments.
     direction -- asc: ascending,
                  desc (default): descending.
     since     -- String ISO 8601 timestamp."
  [options]
  (api-call :get "issues" nil (join-labels options)))

(defn repo-issues
  "List issues for a repository.
   Options are:
     milestone -- Milestone number,
                  none: no milestone,
                  *: any milestone.
     assignee  -- A username,
                  none: no assigned user,
                  *: any assigned user.
     mentioned -- A username.
     state     -- open (default), closed.
     labels    -- A string of comma-separated label names.
     sort      -- created (default), updated, comments.
     direction -- asc: ascending,
                  desc (default): descending.
     since     -- String ISO 8601 timestamp."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/issues" [user repo] (join-labels options)))

(defn specific-issue
  "Fetch a specific issue."
  [user repo number]
  (api-call :get "repos/%s/%s/issues/%s" [user repo number] nil))

(defn create-issue
  [user repo title options]
  "Create an issue.
   Options are:
     milestone -- Milestone number to associate with this issue..
     assignee  -- A username to assign to this issue.
     labels    -- A list of labels to associate with this issue.
     body      -- The body text of the issue."
  (api-call :post "repos/%s/%s/issues" [user repo] (assoc options :title title)))

(defn edit-issue
  [user repo id options]
  "Edit an issue.
   Options are:
     milestone -- Milestone number to associate with this issue..
     assignee  -- A username to assign to this issue.
     labels    -- A list of labels to associate with this issue.
                  Replaces the existing labels.
     state     -- open or closed.
     title     -- Title of the issue.
     body      -- The body text of the issue."
  (api-call :post "repos/%s/%s/issues/%s" [user repo id] options))

;; ## Issue omments API

(defn comments
  [user repo id]
  "List comments on an issue."
  (api-call :get "repos/%s/%s/issues/%s/comments" [user repo id] nil))

(defn specific-comment
  [user repo comment-id]
  "Get a specific comment."
  (api-call :get "repos/%s/%s/issues/comments/%s" [user repo comment-id] nil))

(defn create-comment
  "Create a comment."
  [user repo id body options]
  (api-call :post "repos/%s/%s/issues/%s/comments"
            [user repo id] (assoc options :body body)))

(defn edit-comment
  "Edit a comment."
  [user repo comment-id body options]
  (api-call :post "repos/%s/%s/issues/comments/%s"
            [user repo comment-id] (assoc options :body body)))

(defn delete-comment
  "Delete a comment."
  [user repo comment-id options]
  (api-call :delete "repos/%s/%s/issues/comments/%s" [user repo comment-id] options))

;; ## Issue Event API

(defn events
  "List events for an issue."
  [user repo id]
  (api-call :get "repos/%s/%s/issues/%s/events" [user repo id] nil))

(defn repo-events
  "List events for a repository."
  [user repo]
  (api-call :get "repos/%s/%s/issues/events" [user repo] nil))

(defn specific-event
  "Get a single, specific event."
  [user repo id]
  (api-call :get "repos/%s/%s/issues/events/%s" [user repo id] nil))