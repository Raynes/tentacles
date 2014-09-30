(ns tentacles.search-syntax)

(defn unwind-query
  [key value]
  (letfn [(gen-str
            [k v]
            (if (= k :text) v (str (name k) ":" v)))]
    (if (sequential? value)
      (->> value
           (map (fn [v] (gen-str key v)))
           (clojure.string/join "+"))
      (gen-str key value))))

(defn generate-query-string
  [query]
  (if (empty? query)
    nil
    (->> query
       (map (fn [[k v]] (unwind-query k v)))
       (clojure.string/join "+"))))

(defn assoc-not-nil-value
  [coll key value]
  (if (nil? value) coll (assoc coll key value)))
