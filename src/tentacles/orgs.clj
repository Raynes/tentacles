(ns tentacles.orgs
  "Implements the Github Orgs API: http://developer.github.com/v3/orgs/"
  (:use [tentacles.core :only [api-call]]))

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
