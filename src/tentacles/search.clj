(ns tentacles.search
  "Implements the Github Search API: http://developer.github.com/v3/search/"
  (:use [tentacles.core :only [api-call]]
        tentacles.search-syntax))

(def ^{:private true} default-result-handler :items)

(defn- search [path keywords query options result-handler]
  (let [handler (or result-handler default-result-handler)
        result (api-call :get
                         path
                         nil
                         (assoc options :q (query-str keywords query)))
        handled-result (handler result)]
    (if (nil? handled-result) result handled-result)))

(defn search-repositories-v3
  "Find repositories using GitHub Search API v3"
  [keywords & [query options result-handler]]
  (search "search/repositories" keywords query options result-handler))

(defn search-issues-v3
  "Find issues using GitHub Search API v3"
  [keywords & [query options result-handler]]
  (search "search/issues" keywords query options result-handler))

(defn search-code-v3
  "Find file contents using GitHub Search API v3"
  [keywords & [query options result-handler]]
  (search "search/code" keywords query options result-handler))

(defn search-users-v3
  "Find users using GitHub Search API v3"
  [keywords & [query options result-handler]]
  (search "search/users" keywords query options result-handler))

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

(defn search-code
  "Find file contents via various criteria. This method returns up to 100
  results per page.
  Parameters are:
    q: string - The search terms. I.e: 'defn mymethod in:file language:cljj'
    sort: string (optional) - Sort field, defaults to best match,
    order: string (optional) - Sort order if sort parameter is provided.

  i.e: (search/search-code \"addClass in:file language:js repo:jquery/jquery\")

  More details about the search terms syntax in:
  http://developer.github.com/v3/search/#search-code"
  [query & [sort order options]]
  (let [results (api-call :get "search/code" nil
                          (assoc options
                                 :q query
                                 :sort sort
                                 :order order))]
    (or (:code results)
        results)))
