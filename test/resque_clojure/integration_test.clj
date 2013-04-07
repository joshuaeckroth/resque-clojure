(ns resque-clojure.integration-test
  (:use [clojure.test])
  (:require [resque-clojure.core :as resque]
            [resque-clojure.redis :as redis]
            [resque-clojure.test-helper :as helper]))

(use-fixtures :once helper/redis-test-instance)

(def our-list (atom []))
(defn add-to-list [& args]
  (swap! our-list into args))

(deftest test-single-queue-integration
  (reset! our-list [])
  (resque/enqueue "test-queue" "resque-clojure.integration-test/add-to-list" "one" 2 3)
  (resque/start ["test-queue"])
  (Thread/sleep 50)
  (resque/stop)
  (is (= [2 3 "one"] (sort-by str @our-list))))

(deftest test-multiple-queue-integration
  (reset! our-list [])
  (resque/enqueue "test-queue" "resque-clojure.integration-test/add-to-list" "one" 2 3)
  (resque/enqueue "test-queu2" "resque-clojure.integration-test/add-to-list" "four")
  (resque/start ["test-queue" "test-queu2"])
  (Thread/sleep 50)
  (resque/stop)
  (is (= [2 3 "four" "one"] (sort-by str @our-list))))
