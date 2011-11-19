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

(defn user-issues
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
  [user repo number & [options]]
  (api-call :get "repos/%s/%s/issues/%s" [user repo number] options))

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

;; ## Issue Comments API

(defn issue-comments
  [user repo id & [options]]
  "List comments on an issue."
  (api-call :get "repos/%s/%s/issues/%s/comments" [user repo id] options))

(defn specific-comment
  [user repo comment-id & [options]]
  "Get a specific comment."
  (api-call :get "repos/%s/%s/issues/comments/%s" [user repo comment-id] options))

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
  (nil?
   (api-call :delete "repos/%s/%s/issues/comments/%s"
             [user repo comment-id] options)))

;; ## Issue Event API

(defn issue-events
  "List events for an issue."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/issues/%s/events" [user repo id] options))

(defn repo-events
  "List events for a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/issues/events" [user repo] options))

(defn specific-event
  "Get a single, specific event."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/issues/events/%s" [user repo id] options))

;; ## Issue Label API

(defn repo-labels
  "List labels for a repo."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/labels" [user repo] options))

(defn issue-labels
  "List labels on an issue."
  [user repo issue-id & [options]]
  (api-call :get "repos/%s/%s/issues/%s/labels" [user repo issue-id] options))

(defn specific-label
  "Get a specific label."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/labels/%s" [user repo id] options))

(defn create-label
  "Create a label."
  [user repo name color options]
  (api-call :post "repos/%s/%s/labels"
            [user repo] (assoc options :name name :color color)))

(defn edit-label
  "Edit a label."
  [user repo id name color options]
  (api-call :post "repos/%s/%s/labels/%s"
            [user repo id] (assoc options :name name :color color)))

(defn delete-label
  "Delete a label."
  [user repo id options]
  (nil? (api-call :delete "repos/%s/%s/labels/%s" [user repo id] options)))

(defn add-labels
  "Add labels to an issue."
  [user repo issue-id labels options]
  (api-call :post "repos/%s/%s/issues/%s/labels"
            [user repo issue-id] (assoc options :raw labels)))

(defn remove-label
  "Remove a label from an issue."
  [user repo issue-id label-id options]
  (api-call :delete "repos/%s/%s/issues/%s/labels/%s"
            [user repo issue-id label-id] options))

(defn replace-labels
  "Replace all labels for an issue."
  [user repo issue-id labels options]
  (api-call :put "repos/%s/%s/issues/%s/labels"
            [user repo issue-id] (assoc options :raw labels)))

(defn remove-all-labels
  "Remove all labels from an issue."
  [user repo issue-id options]
  (nil? (api-call :delete "repos/%s/%s/issues/%s/labels" [user repo issue-id] options)))

(defn milestone-labels
  "Get labels for every issue in a milestone."
  [user repo stone-id & [options]]
  (api-call :get "repos/%s/%s/milestones/%s/labels" [user repo stone-id] options))

;; ## Issue Milestones API

(defn repo-milestones
  "List milestones for a repository.
   Options are:
     state     -- open (default), closed.
     direction -- asc, desc (default).
     sort      -- due_date (default), completeness."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/milestones" [user repo] options))

(defn specific-milestone
  "Get a specific milestone."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/milestones/%s" [user repo id] options))

(defn create-milestone
  "Create a milestone.
   Options are:
     state       -- open (default), closed.
     description -- a description string.
     due-on      -- String ISO 8601 timestamp"
  [user repo title options]
  (api-call :post "repos/%s/%s/milestones"
            [user repo] (assoc options :title title)))

(defn edit-milestone
  "Edit a milestone.
   Options are:
     state       -- open (default), closed.
     description -- a description string.
     due-on      -- String ISO 8601 timestamp"
  [user repo id title options]
  (api-call :post "repos/%s/%s/milestones/%s"
            [user repo id] (assoc options :title title)))

(defn delete-milestone
  "Delete a milestone."
  [user repo id options]
  (nil? (api-call :delete "repos/%s/%s/milestones/%s" [user repo id] options)))