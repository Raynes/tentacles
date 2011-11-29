(ns tentacles.orgs
  "Implements the Github Orgs API: http://developer.github.com/v3/orgs/"
  (:use [tentacles.core :only [api-call]]
        [slingshot.slingshot :only [try+]]))

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

(defn edit-org
  "Edit an organization.
   Options are:
      billing-email -- Billing email address.
      company       -- The name of the company the organization belongs to.
      email         -- Publically visible email address.
      location      -- Organization location.
      name          -- Name of the organization."
  [org options]
  (api-call :post "orgs/%s" [org] options))

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
  (try+
   (nil? (api-call :get "orgs/%s/members/%s" [org user] options))
   (catch [:status 404] _ false)))

(defn delete-member
  "Remove a member from all teams and eliminate access to the organization's
   repositories."
  [org user options]
  (nil? (api-call :delete "orgs/%s/members/%s" [org user] options)))

;; `members` already does this if you aren't authenticated, but for the sake of being
;; complete...
(defn public-members
  "List the public members of an organization."
  [org & [options]]
  (api-call :get "orgs/%s/public_members" [org] options))

(defn public-member?
  "Check if a user is a public member or not."
  [org user & [options]]
  (try+
   (nil? (api-call :get "orgs/%s/public_members/%s" [org user] options))
   (catch [:status 404] _ false)))

(defn publicize
  "Make a user public."
  [org user options]
  (nil? (api-call :put "orgs/%s/public_members/%s" [org user] options)))

(defn conceal
  "Conceal a user's membership."
  [org user options]
  (nil? (api-call :delete "orgs/%s/public_members/%s" [org user] options)))