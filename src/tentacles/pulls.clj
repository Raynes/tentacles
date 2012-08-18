(ns tentacles.pulls
  "Implement the Github Pull Requests API: http://developer.github.com/v3/pulls/"
  (:refer-clojure :exclude [merge])
  (:use [tentacles.core :only [api-call no-content?]]))

(defn pulls
  "List pull requests on a repo.
   Options are:
      state -- open (default), closed."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/pulls" [user repo] options))

(defn specific-pull
  "Get a specific pull request."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/pulls/%s" [user repo id] options))

(defn create-pull
  "Create a new pull request. If from is a number, it is considered
   to be an issue number on the repository in question. If this is used,
   the pull request will be created from the existing issue. If it is a
   string it is considered to be a title. base is the branch or ref that
   you want your changes pulled into, and head is the branch or ref where
   your changes are implemented.
   Options are:
      body -- The body of the pull request text. Only applies when not
              creating a pull request from an issue."
  ([user repo from base head options]
     (api-call :post "repos/%s/%s/pulls" [user repo]
               (let [base-opts (assoc options
                                 :base base
                                 :head head)]
                 (if (number? from)
                   (assoc base-opts :issue from)
                   (assoc base-opts :title from))))))

(defn edit-pull
  "Edit a pull request.
   Options are:
      title -- a new title.
      body  -- a new body.
      state -- open or closed."
  [user repo id options]
  (api-call :post "repos/%s/%s/pulls/%s" [user repo id] options))

(defn commits
  "List the commits on a pull request."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/pulls/%s/commits" [user repo id] options))

(defn files
  "List the files on a pull request."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/pulls/%s/files" [user repo id] options))

(defn merged?
  "Check if a pull request has been merged."
  [user repo id & [options]]
  (no-content? (api-call :get "repos/%s/%s/pulls/%s/merge" [user repo id] options)))

(defn merge
  "Merge a pull request.
   Options are:
      commit-message -- A commit message for the merge commit."
  [user repo id options]
  (api-call :put "repos/%s/%s/pulls/%s/merge" [user repo id] options))

;; ## Pull Request Comment API

(defn comments
  "List comments on a pull request."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/pulls/%s/comments" [user repo id] options))

(defn specific-comment
  "Get a specific comment on a pull request."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/pulls/comments/%s" [user repo id] options))

;; You're supposed to be able to reply to comments as well, but that doesn't seem
;; to actually work. Commenting tha
(defn create-comment
  "Create a comment on a pull request."
  [user repo id sha path position body options]
  (api-call :post "repos/%s/%s/pulls/%s/comments" [user repo id]
            (assoc options
              :commit-id sha
              :path path
              :position position
              :body body)))

(defn edit-comment
  "Edit a comment on a pull request."
  [user repo id body options]
  (api-call :post "repos/%s/%s/pulls/comments/%s" [user repo id]
            (assoc options :body body)))

(defn delete-comment
  "Delete a comment on a pull request."
  [user repo id options]
  (no-content? (api-call :delete "repos/%s/%s/pulls/comments/%s" [user repo id] options)))
