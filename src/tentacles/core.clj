;; The v3 version of the Github API is very simple and consistent. We're going to take
;; the mini-functional-DSL approach to this. We'll just abstract over API requests and
;; write a function for every API call. This results in a simple and consistent implementation
;; that requires no macro-magic.
(ns tentacles.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def url "https://api.github.com/")

;; The Github API expects json with underscores and such. cheshire can
;; turn our keywords into strings, but it doesn't replace hyphens with
;; underscores. We'll use this little interim step to do it ourselves.
(defn query-map
  "Turn keywords into strings and replace hyphens with underscores."
  [entries]
  (into {}
        (for [[k v] entries]
          [(.replace (name k) "-" "_") v])))

;; Some Github API responses are 204 and thus have no body. We can't
;; parse those as JSON, so we'll check for that first.
(defn safe-parse [req]
  (when-not (= 204 (:status req))
    (json/parse-string (:body req) true)))

;; We're using basic auth for authentication because oauth2 isn't really that
;; easy to work with, and isn't really applicable to desktop applications (at
;; this point, Github itself recommends basic auth for desktop apps).
;;
;; Each function will pass positional arguments and possibly a map of query
;; args or a map that will get transformed into a JSON hash and sent as the
;; body of POST requests. The positional arguments are formatted into the
;; URL itself, where the URL has placed %s where it needs to have the args
;; placed. If the method is :put or :post, we generate JSON from query OR
;; if query is a map that contains a :raw key, we generate JSON from that.
;; This allows us to handle API calls that expect the body to not be a hash
;; but instead be some other JSON object. Doing this allows us to be
;; consistent in expecting a map.
;;
;; The query map can also contain an :auth key (that will be removed from the
;; map before it is used as query params or JSON data). This is either a
;; string like "username:password" or a vector like ["username" "password"].
;; Authentication is basic authentication.
(defn make-request [method end-point positional query]
  (let [req (merge
             {:url (str url (apply format end-point positional))
              :basic-auth (query "auth")
              :throw-exceptions false
              :method method}
             (when (query "oauth_token")
               {:headers {"Authorization" (str "token " (query "oauth_token"))}}))]
    (safe-parse
     (http/request
      (let [proper-query (dissoc query "auth" "oauth_token")]
        (if (#{:post :put :delete} method)
          (assoc req :body (json/generate-string (or (proper-query "raw") proper-query)))
          (assoc req :query-params proper-query)))))))

;; Functions will call this to create API calls.
(defn api-call [method end-point positional query]
  (let [query (query-map query)]
    (make-request method end-point positional query)))
