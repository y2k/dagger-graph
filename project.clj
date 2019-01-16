(defproject module-graph "0.1"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.jsoup/jsoup "1.11.3"]]
  :plugins [[lein-cljfmt "0.6.3"]]
  :main module-graph.core
  :aot [module-graph.core]
  :profiles {:dev {:dependencies [[cljfmt "0.6.3"]]}}
  :repl-options {:init-ns module-graph.core})
