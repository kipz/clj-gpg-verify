(ns leiningen.gpg-verify
  (:require [antq.dep.leiningen :as lein]
            [org.kipz.gpg-verify.core :as core]
            [clojure.string :as str]
            [leiningen.core.main :as log]))



(defn gpg-verify
  "Verify GPG signatures of specific artifacts"
  [project & args]
  (if-let [names (not-empty (:deps (:gpg-verify project)))]
    (let [to-verify (filter
                     #(some
                       (fn [named]
                         (str/starts-with? (:name %) (str named)))
                       names)
                     (lein/load-deps "."))]
      (log/info "Verifying" (str/join ", " (map str names)))
     (doseq [dep to-verify
             artifact (core/download-artifacts dep)]
       (core/verify-artifact artifact)
       (log/info "Signature Verified:" (.getName (.getFile artifact)))))
    (log/warn "Add dependencies to project.clj like {:gpg-verify {:deps [[clojure/clojure]]}}")))