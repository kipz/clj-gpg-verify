# clj-gpg-verify

[![Clojars Project](https://img.shields.io/clojars/v/org.kipz/clj-gpg-verify.svg)](https://clojars.org/org.kipz/clj-gpg-verify)

Verify tools deps/lein dependency gpg signatures via allow-list

Uses https://github.com/liquidz/antq for much of file/artifact wrangling, but also
supports using environment based repo credentials from project.clj

## Usage

### Clojure Tools Deps

Add the following to your deps.edn:

```clojure

  :gpg-verify {:exec-fn org.kipz.gpg-verify.clojure/gpg-verify
               :exec-args {:verify [mycommany/internal-cljj]}
               :extra-deps {org.kipz/clj-gpg-verify {:mvn/version "0.1.2"}}}
```

### Leiningen

Put `[clj-gpg-verify "0.1.2"]` into the `:plugins` vector of your `:user`
profile or in the `:plugins` of your poject.clj:

Add a list of dependencies to verify to your project.clj:

```clojure
  :gpg-verify {:deps [mycommany/internal-clj]}
```

Then run:

```shell
$ lein gpg-verify
```

## License

Copyright © 2021 James Carnegie

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
