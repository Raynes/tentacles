(ns tentacles.repos
  "Implements the Github Repos API: http://developer.github.com/v3/repos/"
  (:use [tentacles.core :only [api-call]]))

;; ## Primary Repos API

(defn repos
  "List the authenticated user's repositories.
   Options are:
      type -- all (default), public, private, member."
  [options]
  (api-call :get "user/repos" nil options))

(defn user-repos
  "List a user's repositories.
   Options are:
      types -- all (default), public, private, member."
  [user & [options]]
  (api-call :get "users/%s/repos" [user] options))

(defn org-repos
  "List repositories for an organization.
   Options are:
      type -- all (default), public, private."
  [org & [options]]
  (api-call :get "orgs/%s/repos" [org] options))

(defn create-repo
  "Create a new repository.
   Options are:
      description   -- Repository's description.
      homepage      -- Link to repository's homepage.
      public        -- true (default), false.
      has-issues    -- true (default), false.
      has-wiki      -- true (default), false.
      has-downloads -- true (default), false."
  [name options]
  (api-call :post "user/repos" nil (assoc options :name name)))

(defn create-org-repo
  "Create a new repository in an organization..
   Options are:
      description   -- Repository's description.
      homepage      -- Link to repository's homepage.
      public        -- true (default), false.
      has-issues    -- true (default), false.
      has-wiki      -- true (default), false.
      has-downloads -- true (default), false.
      team-id       -- Team that will be granted access to this
                       repository."
  [org name options]
  (api-call :post "orgs/%s/repos" [org] (assoc options :name name)))

(defn specific-repo
  "Get a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s" [user repo] options))

(defn edit-repo
  "Edit a repository.
   Options are:
      description   -- Repository's description.
      name          -- Repository's name.
      homepage      -- Link to repository's homepage.
      public        -- true, false.
      has-issues    -- true, false.
      has-wiki      -- true, false.
      has-downloads -- true, false."
  [user repo options]
  (api-call :post "repos/%s/%s"
            [user repo]
            (if (:name options)
              options
              (assoc options :name repo))))

(defn contributors
  "List the contributors for a project.
   Options are:
      anon -- true, false (default): If true, include
              anonymous contributors."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/contributors" [user repo] options))

(defn languages
  "List the languages that a repository uses."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/languages" [user repo] options))

(defn teams
  "List a repository's teams."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/teams" [user repo] options))

(defn tags
  "List a repository's tags."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/tags" [user repo] options))

(defn branches
  "List a repository's branches."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/branches" [user repo] options))
