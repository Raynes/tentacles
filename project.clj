(defproject tentacles "0.5.1"
  :description "A library for working with the Github API."
  :url "https://github.com/Raynes/tentacles"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http "2.2.0"]
                 [cljs-http "0.1.41"]
                 [cheshire "5.4.0"]
                 [com.cemerick/url "0.1.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [environ "1.0.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/clojurescript "1.8.51"]]
                   :plugins      [[lein-cljsbuild "1.0.5"]
                                  [lein-doo "0.1.7"]]}}
  :doo {:build "test"
        :alias {:default [:slimer]}}
  :cljsbuild {:builds {:test
                       {:source-paths ["src" "test"]
                        :compiler     {:main      tentacles.runner
                                       :output-to "target/tentacles-test.js"}}}})
