(ns tentacles.issues
  (:use [tentacles.core :only [api-call]]
        [clojure.string :only [join]]))

(defn- join-labels [m]
  (if (:labels m)
    (update-in m [:labels] (partial join ","))
    m))

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
  [& [options]]
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
  [user repo title & [options]]
  "Create an issue.
   Options are:
     milestone -- Milestone number to associate with this issue..
     assignee  -- A username to assign to this issue.
     labels    -- A list of labels to associate with this issue.
     body      -- The body text of the issue."
  (api-call :post "repos/%s/%s/issues" [user repo] options :title title))

(defn edit-issue
  [user repo id & [options]]
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