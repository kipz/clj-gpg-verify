(ns leiningen.gpg-verify
  (:require [antq.dep.leiningen :as lein]
            [org.kipz.gpg-verify.core :as core]
            [clojure.string :as str]
            [antq.record :as r]
            [antq.util.dep :as u.dep]
            [leiningen.core.main :as log]
            [clojure.walk :as walk]))


(defn extract-deps
  "TODO - extacted from antq - create PR to antq?"
  [file-path project-clj-content-str]

  (let [dep-form? (atom false)
        repos-form? (atom false)
        deps (atom [])
        repos (atom [])]
    (walk/prewalk (fn [form]
                    (cond
                      (keyword? form)
                      (do (reset! dep-form? (#{:dependencies :plugins} form))
                          (reset! repos-form? (= :repositories form)))

                      (and @dep-form?
                           (sequential? form)
                           (sequential? (first form)))
                      (swap! deps concat form)

                      (and @repos-form?
                           (sequential? form)
                           (sequential? (first form)))
                      (swap! repos concat form))
                    form)
                  (read-string (str "(list " project-clj-content-str " )")))
    (let [repositories (reduce (fn [acc [k v]] (assoc acc k (if (map? v) v {:url v})))  {} @repos)]
      (for [[dep-name version] @deps
            :when (and (string? version) (seq version))]
        (r/map->Dependency {:project :leiningen
                            :type :java
                            :file file-path
                            :name  (if (u.dep/qualified-symbol?' dep-name)
                                     (str dep-name)
                                     (str dep-name "/" dep-name))
                            :version version
                            :repositories repositories})))))

(defn gpg-verify
  "Verify GPG signatures of specific artifacts"
  [project & args]
  (if-let [names (not-empty (:deps (:gpg-verify project)))]
    (let [to-verify (filter
                     #(some
                       (fn [named]
                         (str/starts-with? (:name %) (str named)))
                       names)
                     (extract-deps "project.clj" (pr-str project)))]
      (log/info "Verifying" (str/join ", " (map str names)))
      (doseq [dep to-verify
              artifact (core/download-artifacts dep)]
        (core/verify-artifact artifact)
        (log/info "Signature Verified:" (.getName (.getFile artifact)))))
    (log/warn "Add dependencies to project.clj like {:gpg-verify {:deps [clojure/clojure]}}")))