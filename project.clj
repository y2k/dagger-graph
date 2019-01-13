(defproject module-graph "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :plugins [[lein-cljfmt "0.6.3"]]
  :main module-graph.core
  :aot [module-graph.core]
  :profiles {:dev {:dependencies [[cljfmt "0.6.3"]]}}
  :repl-options {:init-ns module-graph.core})
