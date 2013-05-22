(ns resque-clojure.worker-test
  (:refer-clojure :exclude [name])
  (:use [resque-clojure.worker]
        [clojure.test]))

(deftest lookup-fn-test
  (is (= #'clojure.core/str (lookup-fn "clojure.core/str"))))

(defn exceptional [& args] (/ 1 0))

(deftest work-on-test
  (let [good-job {:func "clojure.core/str" :args ["foo"] :queue "test-queue"}
        bad-job {:func "resque-clojure.worker-test/exceptional" :args ["foo"]}]
    (is (= {:name "worker" :result :pass :job good-job :queue "test-queue"} (work-on {:name "worker"} good-job)))
    (is (= :error (:result (work-on {:name "worker"} bad-job))))
    (is (= java.lang.ArithmeticException (.getClass (:exception (work-on {:name "worker"} bad-job)))))))

