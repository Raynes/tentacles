(ns tentacles.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [tentacles.core-test]
            [tentacles.gists-test]
            [tentacles.search-test]))

(doo-tests 'tentacles.core-test
           'tentacles.gists-test
           'tentacles.search-test)