(ns tentacles.data
  "Implements the Git Data API: http://developer.github.com/v3/git/blobs/"
  (:use [tentacles.core :only [api-call]]))

;; ## Blobs

(defn blob
  "Get a blob."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/git/blobs/%s" [user repo sha] options))

(defn create-blob
  "Create a blob."
  [user repo content encoding options]
  (api-call :post "repos/%s/%s/git/blobs" [user repo]
            (assoc options
              :content content
              :encoding encoding)))

;; ## Commits

(defn commit
  "Get a commit."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/git/commits/%s" [user repo sha] options))

(defn create-commit
  "Create a commit.
   Options are:
      parents         -- A sequence of SHAs of the commits that were
                         the parents of this commit. If omitted, the
                         commit will be written as a root commit.
      author.name     -- Name of the author of the commit.
      author.email    -- Email of the author of the commit.
      author.date     -- Timestamp when this commit was authored.
      committer.name  -- The name of the committer of this commit.
      committer.email -- The email of the committer of this commit.
      committer.date  -- Timestamp when this commit was committed.
   If the committer section is omitted, then it will be filled in with author
   data. If that is omitted, information will be obtained using the
   authenticated user's information and the current date."
  [user repo message tree options]
  (api-call :post "repos/%s/%s/git/commits" [user repo]
            (assoc options
              :message message
              :tree tree
              :parents parents)))

