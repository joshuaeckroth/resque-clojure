(ns resque-clojure.integration-test
  (:use [clojure.test])
  (:require [resque-clojure.core :as core]
            [resque-clojure.supervisor :as supervisor]
            [resque-clojure.resque :as resque]
            [resque-clojure.redis :as redis]
            [resque-clojure.test-helper :as helper]))

(use-fixtures :once helper/redis-test-instance)

(def our-list (atom []))

(defn add-to-list [& args]
  (Thread/sleep 300)
  (swap! our-list into args))

(deftest test-single-queue-integration
  (reset! our-list [])
  (core/enqueue "test-queue" "resque-clojure.integration-test/add-to-list" "one" 2 3)
  (is (= 0 (count @supervisor/idle-agents)))
  (core/start ["test-queue"])
  (is (= 1 (count @supervisor/working-agents)))
  (Thread/sleep 500)
  (is (= 1 (count @supervisor/idle-agents)))
  (core/stop)
  (is (= 0 (count @supervisor/idle-agents)))
  (is (= [2 3 "one"] (sort-by str @our-list))))

(deftest test-multiple-queue-integration
  (reset! our-list [])
  (core/enqueue "test-queue" "resque-clojure.integration-test/add-to-list" "one" 2 3)
  (core/enqueue "test-queu2" "resque-clojure.integration-test/add-to-list" "four")
  (core/start ["test-queue" "test-queu2"])
  (Thread/sleep 500)
  (core/stop)
  (is (= [2 3 "four" "one"] (sort-by str @our-list))))

