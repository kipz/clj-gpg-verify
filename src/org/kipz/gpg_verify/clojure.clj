(ns org.kipz.gpg-verify.clojure
  (:require
   [org.kipz.gpg-verify.core :as core]
   [antq.dep.clojure :as clojure]
   [clojure.string :as str]))
(defn gpg-verify
  [args]
  (if-let [names (not-empty (:verify args))]
    (let [to-verify (filter
                     #(some
                       (fn [named]
                         (str/starts-with? (:name %) (str named)))
                       names)
                     (clojure/load-deps "."))]
      (println "Verifying" (str/join ", " (map str names)))
      (doseq [dep to-verify
              artifact (core/download-artifacts dep)]
        (core/verify-artifact artifact)
        (println "Signature Verified:" (.getName (.getFile artifact))))
      (System/exit 0))
    (println "Add dependencies to project.clj like {:verify [clojure/clojure]}")))