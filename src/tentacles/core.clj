(ns tentacles.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def url "https://api.github.com/")

(defn query-map
  "Turn keywords into strings and replace hyphens with underscores."
  [entries]
  (into {}
        (for [[k v] entries]
          [(.replace (name k) "-" "_") v])))

(defn make-request [method end-point positional query]
  (let [req {:url (str url (apply format end-point positional))
             :basic-auth (query "auth")
             :method method}]
    (-> (http/request
         (let [proper-query (dissoc query "auth")]
           (if (= method :post)
             (assoc req :body (json/generate-string proper-query))
             (assoc req :query-params proper-query))))
        :body
        (json/parse-string true))))

(defn api-call [method end-point positional query & keyed-required]
  (let [query (query-map (partition 2 (concat query keyed-required)))]
    (make-request method end-point positional query)))
