(ns tentacles.search
  "Implements the Github Search API: http://developer.github.com/v3/search/"
  (:use [tentacles.core :only [api-call]]
        [clojure.string :refer [join]]))

(def ^{:private true} separator " ")

(defn- unwind-query
  [key value]
  (letfn [(gen-str
            [k v]
            (str (name k) ":" v))]
    (cond
      (sequential? value) (->> value
                               (map #(gen-str key %))
                               (join separator))
      (nil? value) nil
      :else (gen-str key value))))

(defn search-term
  "Builds search term based on keywords and qualifiers."
  [keywords & [query]]
  (let [joined-keywords (if (sequential? keywords) (join separator keywords) keywords)]
    (if (empty? query)
      joined-keywords
      (->> query
           (map (fn [[k v]] (unwind-query k v)))
           (into [joined-keywords])
           (filter (comp not nil?))
           (join separator)))))

(defn- search [path keywords query options]
  (api-call :get
            path
            nil
            (assoc options :q (search-term keywords query))))

(defn search-repos
  "Find repositories via various criteria. This method returns up to 100
  results per page.

  Parameters are:
    keywords - The search keywords. Can be string or sequence of strings.
    query - The search qualifiers. Query is a map that contains qualifiers
      values where key is a qualifier name.

  The query map can contains any combination of the supported repository
  search qualifiers. See full list in:
    https://developer.github.com/v3/search/#search-repositories

  Sort and order fields are available via the options map.

  This method follows API v3 pagination rules. More details about
  pagination rules in: https://developer.github.com/v3/#pagination

  Returns map with the following elements:
    :total_count - The total number of found items.
    :incomplete_results - true if query exceeds the time limit.
    :items - The result vector of found items.

  Example:
  (search-repos \"tetris\"
              {:language \"assembly\"}
              {:sort \"stars\" :order \"desc\"})

  This corresponds to the following search term:
  https://api.github.com/search/repositories?
    q=tetris+language:assembly&sort=stars&order=desc"
  [keywords & [query options]]
  (search "search/repositories" keywords query options))

(defn search-code
  "Find file contents via various criteria. This method returns up to 100
  results per page.

  Parameters are:
    keywords - The search keywords. Can be string or sequence of strings.
    query - The search qualifiers. Query param is a map that contains qualifiers
      values where key is a qualifier name.

  The query map can contains any combination of the supported repository
  search qualifiers. See full list in:
    https://developer.github.com/v3/search/#search-code

  Sort and order fields are available via the options map.

  This method follows API v3 pagination rules. More details about
  pagination rules in: https://developer.github.com/v3/#pagination

  Returns map with the following elements:
    :total_count - The total number of found items.
    :incomplete_results - true if query exceeds the time limit.
    :items - The result vector of found items.

  Example:
  (search-code \"addClass\"
              {:in \"file\" :language \"js\" :repo \"jquery/jquery\"})

  This corresponds to the following search term:
  https://api.github.com/search/code?
    q=addClass+in:file+language:js+repo:jquery/jquery"
  [keywords & [query options]]
  (search "search/code" keywords query options))

(defn search-issues
  "Find issues by state and keyword. This method returns up to 100
  results per page.

  Parameters are:
    keywords - The search keywords. Can be string or sequence of strings.
    query - The search qualifiers. Query is a map that contains qualifiers
      values where key is a qualifier name.

  The query map can contains any combination of the supported repository
  search qualifiers. See full list in:
    https://developer.github.com/v3/search/#search-issues

  Sort and order fields are available via the options map.

  This method follows API v3 pagination rules. More details about
  pagination rules in: https://developer.github.com/v3/#pagination

  Returns map with the following elements:
    :total_count - The total number of found items.
    :incomplete_results - true if query exceeds the time limit.
    :items - The result vector of found items.

  Example:
  (search-issues \"windows\"
               {:label \"bug\" :language \"python\" :state \"open\"}
               {:sort \"created\" :order \"asc\"})

  This corresponds to the following search term:
  https://api.github.com/search/issues?
    q=windows+label:bug+language:python+state:open&
    sort=created&order=asc"
  [keywords & [query options]]
  (search "search/issues" keywords query options))

(defn search-users
  "Find users via various criteria. This method returns up to 100
  results per page.

  Parameters are:
    keywords - The search keywords. Can be string or sequence of strings.
    query - The search qualifiers. Query is a map that contains qualifiers
      values where key is a qualifier name.

  The query map can contains any combination of the supported repository
  search qualifiers. See full list in:
    https://developer.github.com/v3/search/#search-users

  Sort and order fields are available via the options map.

  This method follows API v3 pagination rules. More details about
  pagination rules in: https://developer.github.com/v3/#pagination

  Returns map with the following elements:
    :total_count - The total number of found items.
    :incomplete_results - true if query exceeds the time limit.
    :items - The result vector of found items.

  Example:
  (search-users \"tom\" {:repos \">42\" :followers \">1000\"})

  This corresponds to the following search term:
  https://api.github.com/search/users?
    q=tom+repos:%3E42+followers:%3E1000"
  [keywords & [query options]]
  (search "search/users" keywords query options))
