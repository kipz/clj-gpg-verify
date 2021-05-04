(ns org.kipz.gpg-verify.core
  (:require [antq.util.maven :as u.mvn]
            [clojure.string :as str]
            [clojure.java.shell :as shell])

  (:import
   (org.eclipse.aether
    DefaultRepositorySystemSession
    RepositorySystem)
   (org.eclipse.aether.repository RemoteRepository RemoteRepository$Builder)
   (org.eclipse.aether.util.repository AuthenticationBuilder)
   (org.eclipse.aether.util.artifact SubArtifact)
   (org.eclipse.aether.artifact
    Artifact)
   (org.eclipse.aether.resolution
    ArtifactRequest)))

(defn with-creds
  [remote-repos dep]
  (map
   (fn [^RemoteRepository repo]
     (if-let [repo-config (get (:repositories dep) (.getId repo))]
       ;; add in env from project clj if present
       (let [u-env (some->> repo-config :username)
             u-env (if (string? u-env)
                     u-env
                     (some->> u-env (filter #(= "env" (namespace %))) first name (.toUpperCase) (System/getenv)))
             p-env (some->> repo-config :password)
             p-env (if (string? p-env)
                     p-env
                     (some->> p-env (filter #(= "env" (namespace %))) first name (.toUpperCase) (System/getenv)))]

         (if (and u-env p-env)
           (.build (doto (RemoteRepository$Builder. repo)
                     (.setAuthentication (.build (doto (AuthenticationBuilder.)
                                                   (.addUsername u-env)
                                                   (.addPassword p-env))))))
           repo))
       repo))
   remote-repos))

(defn download-artifacts
  "Download artifacts from repo if possible"
  [dep]
  (let [opts (u.mvn/dep->opts dep)
        {:keys [^RepositorySystem system
                ^DefaultRepositorySystemSession  session
                ^Artifact artifact
                remote-repos]} (u.mvn/repository-system (:name dep) (:version dep) opts)
        authed-repos (with-creds remote-repos dep)
        artifacts (->> (conj (for [ext ["pom" "pom.asc" "jar.asc"]]
                               (->> (.resolveArtifact system session (doto (ArtifactRequest.)
                                                                       (.setArtifact (SubArtifact. artifact, nil ext))
                                                                       (.setRepositories authed-repos)))
                                    (.getArtifact)))
                             (->> (.resolveArtifact system session (doto (ArtifactRequest.)
                                                                     (.setArtifact artifact)
                                                                     (.setRepositories authed-repos)))
                                  (.getArtifact)))
                       (remove nil?))]

    (when (not= 4 (count artifacts))
      (throw (IllegalStateException. (format "Could not find all jar.asc and/or pom.asc signature files for %s:%s" (:name dep) (:version dep)))))
    (filter #(str/ends-with? (.getName (.getFile %)) ".asc") artifacts)))

(defn verify-artifact
  [artifact]
  (let [path (-> artifact (.getFile) (.getAbsolutePath))
        verify (shell/sh  "gpg" "--verify" path)]
    (assert (= 0 (:exit verify)) (format "Non-zero exit code from gpg when verifying %s: %s, err: %s" path (:exit verify) (:err verify)))))