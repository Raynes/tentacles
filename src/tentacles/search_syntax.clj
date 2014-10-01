(ns tentacles.search-syntax
  (:use [clojure.string :refer [join]]))

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

(defn query-str
  [keywords & [query]]
  (let [joined-keywords (if (sequential? keywords) (join separator keywords) keywords)]
    (if (empty? query)
      joined-keywords
      (->> query
           (map (fn [[k v]] (unwind-query k v)))
           (into [joined-keywords])
           (filter (comp not nil?))
           (join separator)))))
