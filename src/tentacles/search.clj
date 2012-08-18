(ns tentacles.search
  "Implements the Github Search API: http://developer.github.com/v3/search/"
  (:use [tentacles.core :only [api-call]]))

(defn search-issues
  "Find issues by state and keyword"
  [user repo state keyword & [options]]
  (let [results (api-call :get
                          "/legacy/issues/search/%s/%s/%s/%s"
            			  [user repo state keyword]
            			  options)]
    (or (:issues results)
        results)))

(defn search-repos
  "Find repositories by keyword. This is a legacy method and does not follow
   API v3 pagination rules. It will return up to 100 results per page and pages
   are fetched by passing the start-page parameter.
   Options are:
     start-page: a number. Default is first page.
     language: filter by language"
  [keyword & [options]]
  (let [results (api-call :get "/legacy/repos/search/%s" [keyword] options)]
    (or (:repositories results)
        results)))

(defn search-users
  "Find users by keyword."
  [keyword & [options]]
  (let [results (api-call :get "/legacy/user/search/%s" [keyword] options)]
    (or (:users results)
        results)))