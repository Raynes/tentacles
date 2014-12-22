(ns tentacles.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]
            [cemerick.url :as url]))

(def ^:dynamic url "https://api.github.com/")
(def ^:dynamic defaults {})

(defn query-map
  "Merge defaults, turn keywords into strings, and replace hyphens with underscores."
  [entries]
  (into {}
        (for [[k v] (concat defaults entries)]
          [(.replace (name k) "-" "_") v])))

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

(defn extract-useful-meta
  [h]
  (let [{:strs [etag last-modified x-ratelimit-limit x-ratelimit-remaining
                x-poll-interval]}
        h]
    {:etag etag :last-modified last-modified
     :call-limit (when x-ratelimit-limit (Long/parseLong x-ratelimit-limit))
     :call-remaining (when x-ratelimit-remaining (Long/parseLong x-ratelimit-remaining))
     :poll-interval (when x-poll-interval (Long/parseLong x-poll-interval))}))

(defn api-meta
  [obj]
  (:api-meta (meta obj)))

(defn safe-parse
  "Takes a response and checks for certain status codes. If 204, return nil.
   If 400, 401, 204, 422, 403, 404 or 500, return the original response with the body parsed
   as json. Otherwise, parse and return the body if json, or return the body if raw."
  [{:keys [headers status body] :as resp}]
  (cond
   (= 304 status)
   ::not-modified
   (#{400 401 204 422 403 404 500} status)
   (update-in resp [:body] parse-json)
   :else (let [links (parse-links (get headers "link" ""))
               content-type (get headers "content-type")
               metadata (extract-useful-meta headers)]
           (if-not (.contains content-type "raw")
             (let [parsed (parse-json body)]
               (if (map? parsed)
                 (with-meta parsed {:links links :api-meta metadata})
                 (with-meta (map #(with-meta % metadata) parsed)
                   {:links links :api-meta metadata})))
             body))))

(defn update-req
  "Given a clj-http request, and a 'next' url string, merge the next url into the request"
  [req url]
  (let [url-map (url/url url)]
    (assoc-in req [:query-params] (:query url-map))))

(defn no-content?
  "Takes a response and returns true if it is a 204 response, false otherwise."
  [x] (= (:status x) 204))

(defn format-url
  "Creates a URL out of end-point and positional. Called URLEncoder/encode on
   the elements of positional and then formats them in."
  [end-point positional]
  (str url (apply format end-point (map url/url-encode positional))))

(defn make-request [method end-point positional
                    {:keys [auth throw-exceptions follow-redirects accept
                            oauth-token etag if-modified-since user-agent]
                     :or {follow-redirects true throw-exceptions false}
                     :as query}]
  (let [req (merge-with merge
                        {:url (format-url end-point positional)
                         :basic-auth auth
                         :throw-exceptions throw-exceptions
                         :follow-redirects follow-redirects
                         :method method}
                        (when accept
                          {:headers {"Accept" accept}})
                        (when oauth-token
                          {:headers {"Authorization" (str "token " oauth-token)}})
                        (when etag
                          {:headers {"if-None-Match" etag}})
                        (when user-agent
                          {:headers {"User-Agent" user-agent}})
                        (when if-modified-since
                          {:headers {"if-Modified-Since" if-modified-since}}))
        raw-query (:raw query)
        proper-query (query-map (dissoc query :auth :oauth-token :all-pages :accept :user-agent))
        req (if (#{:post :put :delete} method)
              (assoc req :body (json/generate-string (or raw-query proper-query)))
              (assoc req :query-params proper-query))]
    req))

(defn api-call
  ([method end-point] (api-call method end-point nil nil))
  ([method end-point positional] (api-call method end-point positional nil))
  ([method end-point positional query]
     (let [query (or query {})
           all-pages? (query :all-pages)
           req (make-request method end-point positional query)
           exec-request-one (fn exec-request-one [req]
                              (safe-parse (http/request req)))
           exec-request (fn exec-request [req]
                          (let [resp (exec-request-one req)]
                            (if (and all-pages? (-> resp meta :links :next))
                              (let [new-req (update-req req (-> resp meta :links :next))]
                                (lazy-cat resp (exec-request new-req)))
                              resp)))]
       (exec-request req))))

(defn raw-api-call
  ([method end-point] (raw-api-call method end-point nil nil))
  ([method end-point positional] (raw-api-call method end-point positional nil))
  ([method end-point positional query]
     (let [query (or query {})
           all-pages? (query :all-pages)
           req (make-request method end-point positional query)]
       (http/request req))))

(defn environ-auth
  "Lookup :gh-username and :gh-password in environ (~/.lein/profiles.clj or .lein-env) and return a string auth.
   Usage: (users/me {:auth (environ-auth)})"
  [env]
  (str (:gh-username env ) ":" (:gh-password env)))

(defn rate-limit
  ([] (api-call :get "rate_limit"))
  ([opts] (api-call :get "rate_limit" nil opts)))

(defmacro with-url [new-url & body]
 `(binding [url ~new-url]
    ~@body))

(defmacro with-defaults [options & body]
 `(binding [defaults ~options]
    ~@body))
