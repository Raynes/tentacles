(ns tentacles.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]
            [cemerick.url :as url])
  (:import java.net.URLEncoder))

(def ^:dynamic url "https://api.github.com/")

(defn query-map
  "Turn keywords into strings and replace hyphens with underscores."
  [entries]
  (into {}
        (for [[k v] entries]
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

(defn safe-parse
  "Takes a response and checks for certain status codes. If 204, return nil.
   If 400, 422, 404, 204, or 500, return the original response with the body parsed
   as json. Otherwise, parse and return the body."
  [resp]
  (if (#{400 401 204 422 404 500} (:status resp))
    (update-in resp [:body] parse-json)
    (let [links (parse-links (get-in resp [:headers "link"] ""))]
      (with-meta (parse-json (:body resp)) {:links links}))))

(defn update-req
  "Given a clj-http request, and a 'next' url string, merge the next url into the request"
  [req url]
  (let [url-map (url/url url)]
    (assoc-in req [:query-params] (-> url-map :query))))

(defn no-content?
  "Takes a response and returns true if it is a 204 response, false otherwise."
  [x] (= (:status x) 204))

(defn format-url
  "Creates a URL out of end-point and positional. Called URLEncoder/encode on
   the elements of positional and then formats them in."
  [end-point positional]
  (str url (apply format end-point (map #(URLEncoder/encode % "UTF-8") positional))))

(defn make-request [method end-point positional query]
  (let [all-pages? (query "all_pages")
        req (merge
             {:url (format-url end-point positional)
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

(defn api-call
  ([method end-point] (api-call method end-point nil nil))
  ([method end-point positional] (api-call method end-point positional nil))
  ([method end-point positional query]
   (let [query (query-map query)]
     (make-request method end-point positional query))))

(defn rate-limit [] (api-call :get "rate_limit"))

(defmacro with-url [new-url & body]
 `(binding [url ~new-url]
    ~@body))