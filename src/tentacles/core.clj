;; The v3 version of the Github API is very simple and consistent. We're going to take
;; the mini-functional-DSL approach to this. We'll just abstract over API requests and
;; write a function for every API call. This results in a simple and consistent implementation
;; that requires no macro-magic.
(ns tentacles.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]
            [cemerick.url :as url]))

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

;; cheshire's parse-string explodes on nil input. We'll use this to get around that.
(defn parse-json
  "Same as json/parse-string but handles nil gracefully."
  [s] (when s (json/parse-string s true)))

(defn parse-link [link]
  (let [[_ url] (re-find #"<(.*)>" link)
        [_ rel] (re-find #"rel=\"(.*)\"" link)]
    [(keyword rel) url]))

(defn parse-links
  "Takes the content of the link header from a github resp, returns a map of links"
  [link-body]
  (->> (str/split link-body #",")
       (map parse-link)
       (into {})))

;; Github returns a zillion and one HTTP status codes. Pretty much all of them
;; have JSON bodies, so in the event of something going wrong, we'll return the
;; entire request with the `:body` parsed as JSON. Note that 204s will almost never
;; be translate to the client, since those usually indicate true or false values and
;; are reflected as such by tentacles API fns.
(defn safe-parse
  "Takes a response and checks for certain status codes. If 204, return nil.
   If 400, 422, 404, 204, or 500, return the original response with the body parsed
   as json. Otherwise, parse and return the body."
  [resp]

  (if (#{400 204 422 404 500} (:status resp))
    (update-in resp [:body] parse-json)
    (let [links (parse-links (get-in resp [:headers "link"] ""))]
      (with-meta (parse-json (:body resp)) {:links links}))))

(defn update-req
  "Given a clj-http request, and a 'next' url string, merge the next url into the request"
  [req url]
  (let [url-map (url/url url)]
    (assoc-in req [:query-params] (-> url-map :query))))

;; Github usually throws you 204 responses for 'true' and 404 for 'false'. We want
;; to translate to booleans.
(defn no-content?
  "Takes a response and returns true if it is a 204 response, false otherwise."
  [x] (= (:status x) 204))

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
  (let [all-pages? (query "all_pages")
        req (merge
             {:url (str url (apply format end-point positional))
              :basic-auth (query "auth")
              :throw-exceptions (or (query "throw_exceptions") false)
              :method method}
             (when (query "oauth_token")
               {:headers {"Authorization" (str "token " (query "oauth_token"))}}))
        proper-query (dissoc query "auth" "oauth_token" "all_pages")
        req (if (#{:post :put :delete} method)
              (assoc req :body (json/generate-string (or (proper-query "raw") proper-query)))
              (assoc req :query-params proper-query))
        exec-request-one (fn exec-request-one [req]
                           (safe-parse (http/request req)))
        exec-request (fn exec-request [req]
                       (let [resp (exec-request-one req)]
                         (-> resp meta :links)
                         (if (and all-pages? (-> resp meta :links :next))
                           (let [new-req (update-req req (-> resp meta :links :next))]
                             (lazy-cat resp (exec-request new-req)))
                           resp)))]
    (exec-request req)))

;; Functions will call this to create API calls.
(defn api-call [method end-point positional query]
  (let [query (query-map query)]
    (make-request method end-point positional query)))
