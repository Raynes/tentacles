(ns tentacles.orgs
  "Implements the Github Orgs API: http://developer.github.com/v3/orgs/"
  (:use [tentacles.core :only [api-call no-content?]]))

;; ## Primary API

(defn user-orgs
  "List the public organizations for a user."
  [user]
  (api-call :get "users/%s/orgs" [user] nil))

(defn orgs
  "List the public and private organizations for the currently
   authenticated user."
  [options]
  (api-call :get "user/orgs" nil options))

(defn specific-org
  "Get a specific organization."
  [org & [options]]
  (api-call :get "orgs/%s" [org] options))

(defn repos
  "All repositories in the organization"
  [org & [options]]
  (api-call :get "orgs/%s/repos" [org] options))

(defn edit-org
  "Edit an organization.
   Options are:
      billing-email -- Billing email address.
      company       -- The name of the company the organization belongs to.
      email         -- Publically visible email address.
      location      -- Organization location.
      name          -- Name of the organization."
  [org options]
  (api-call :patch "orgs/%s" [org] options))

;; ## Org Members API

(defn members
  "List the members in an organization. A member is a user that belongs
   to at least one team. If authenticated, both concealed and public members
   will be returned. Otherwise, only public members."
  [org & [options]]
  (api-call :get "orgs/%s/members" [org] options))

(defn member?
  "Check whether or not a user is a member."
  [org user options]
  (no-content? (api-call :get "orgs/%s/members/%s" [org user] options)))

(defn delete-member
  "Remove a member from all teams and eliminate access to the organization's
   repositories."
  [org user options]
  (no-content? (api-call :delete "orgs/%s/members/%s" [org user] options)))

;; `members` already does this if you aren't authenticated, but for the sake of being
;; complete...
(defn public-members
  "List the public members of an organization."
  [org & [options]]
  (api-call :get "orgs/%s/public_members" [org] options))

(defn public-member?
  "Check if a user is a public member or not."
  [org user & [options]]
  (no-content? (api-call :get "orgs/%s/public_members/%s" [org user] options)))

(defn publicize
  "Make a user public."
  [org user options]
  (no-content? (api-call :put "orgs/%s/public_members/%s" [org user] options)))

(defn conceal
  "Conceal a user's membership."
  [org user options]
  (no-content? (api-call :delete "orgs/%s/public_members/%s" [org user] options)))

;; ## Org Teams API

(defn teams
  "List the teams for an organization."
  [org options]
  (api-call :get "orgs/%s/teams" [org] options))

(defn specific-team
  "Get a specific team."
  [id options]
  (api-call :get "teams/%s" [id] options))

(defn create-team
  "Create a team.
   Options are:
      repo-names -- Repos that belong to this team.
      permission -- pull (default): team can pull but not push or admin.
                    push: team can push and pull but not admin.
                    admin: team can push, pull, and admin."
  [org name options]
  (api-call :post "orgs/%s/teams" [org]
            (assoc options
              :name name)))

(defn edit-team
  "Edit a team.
   Options are:
      name        -- New team name.
      permissions -- pull (default): team can pull but not push or admin.
                     push: team can push and pull but not admin.
                     admin: team can push, pull, and admin."
  [id options]
  (api-call :patch "teams/%s" [id] options))

(defn delete-team
  "Delete a team."
  [id options]
  (no-content? (api-call :delete "teams/%s" [id] options)))

(defn team-members
  "List members of a team."
  [id options]
  (api-call :get "teams/%s/members" [id] options))

(defn team-member?
  "Get a specific team member."
  [id user options]
  (no-content? (api-call :get "teams/%s/memberships/%s" [id user] options)))

(defn add-team-member
  "Add a team member."
  [id user options]
  (no-content? (api-call :put "teams/%s/memberships/%s" [id user] options)))

(defn delete-team-member
  "Remove a team member."
  [id user options]
  (no-content? (api-call :delete "teams/%s/memberships/%s" [id user] options)))

(defn list-team-repos
  "List the team repositories."
  [id options]
  (api-call :get "teams/%s/repos" [id] options))

(defn team-repo?
  "Check if a repo is managed by this team."
  [id user repo options]
  (no-content? (api-call :get "teams/%s/repos/%s/%s" [id user repo] options)))

(defn add-team-repo
  "Add a team repo."
  [id user repo options]
  (no-content? (api-call :put "teams/%s/repos/%s/%s" [id user repo] options)))

(defn delete-team-repo
  "Remove a repo from a team."
  [id user repo options]
  (no-content? (api-call :delete "teams/%s/repos/%s/%s" [id user repo] options)))

;; ## Org Hooks API

(defn hooks
  "List the hooks on an organization."
  [org options]
  (api-call :get "orgs/%s/hooks" [org] options))

(defn specific-hook
  "Get a specific hook."
  [org id options]
  (api-call :get "orgs/%s/hooks/%s" [org id] options))

(defn create-hook
  "Create a hook.
   Options are:
      events -- A sequence of event strings. Only 'push' by default.
      active -- true or false; determines if the hook is actually triggered
                on pushes."
  [org config options]
  (api-call :post "orgs/%s/hooks" [org]
            (assoc options
                   :name "web"
                   :config config)))

(defn edit-hook
  "Edit an existing hook.
   Options are:
      config        -- Modified config.
      events        -- A sequence of event strings. Replaces the events.
      active        -- true or false; determines if the hook is actually
                       triggered on pushes."
  [org id options]
  (api-call :patch "orgs/%s/hooks/%s" [org id] (assoc options :name "web")))

(defn ping-hook
  "Ping a hook."
  [org id options]
  (no-content? (api-call :post "orgs/%s/hooks/%s/pings" [org id] options)))

(defn delete-hook
  "Delete a hook."
  [org id options]
  (no-content? (api-call :delete "orgs/%s/hooks/%s" [org id] options)))
