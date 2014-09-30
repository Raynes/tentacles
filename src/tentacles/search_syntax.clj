(ns tentacles.search-syntax)

(defn- unwind-query
  [key value]
  (letfn [(gen-str
            [k v]
            (if (= k :text) v (str (name k) ":" v)))]
    (cond
      (sequential? value) (->> value
                               (map #(gen-str key %))
                               (clojure.string/join " "))
      (nil? value) nil
      :else (gen-str key value))))

(defn query-str
  [query]
  (if (empty? query)
    nil
    (->> query
         (map (fn [[k v]] (unwind-query k v)))
         (filter (comp not nil?))
         (clojure.string/join " "))))
