(ns tentacles.issues
  (:use [tentacles.core :only [api-call add-required]]))

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
  [& options]
  (api-call :get "issues" nil options))

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
  [user repo & options]
  (api-call :get "repos/%s/%s/issues" [user repo] options))

(defn specific-issue
  "Fetch a specific issue."
  [user repo number]
  (api-call :get "repos/%s/%s/issues/%s" [user repo number] nil))

(defn create-issue
  [user repo title & options]
  "TODO"
  (api-call :post "repos/%s/%s/issues" [user repo] options :title title))

(defn edit-issue
  [user repo id & options]
  "TODO"
  (api-call :post "repos/%s/%s/issues/%s" [user repo id] options))