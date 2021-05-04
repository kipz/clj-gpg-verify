(defproject org.kipz/clj-gpg-verify "0.1.3-SNAPSHOT"
  :description "Use gpg to verify GPG signatures of selected clojure project dependecies"
  :url "https://github.com/kipz/clj-gpg-verify"
  :dependencies [[com.github.liquidz/antq "0.13.0"]]
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :deploy-repositories {"releases" {:url "https://repo.clojars.org" :creds :gpg}}
  :eval-in-leiningen true)
