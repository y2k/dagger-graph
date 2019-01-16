(ns module-graph.core
  (:gen-class))

(defn format-to-string [root-name scope-files modules]
  (defn get-children-modules [name all-modules]
    (->> all-modules
         (filter #(= name (:name %)))
         (first)
         (:children-names)
         (sort)))

  (defn rec-format-to-string [module-name prefix a b]
    (str prefix a module-name
         " (" (clojure.string/join ", " (mapv #(:filename %) (filter #(= module-name (:scope %)) scope-files))) ")"
         "\n"
         (let [children (get-children-modules module-name modules)]
           (->> children
                (map-indexed #(rec-format-to-string
                               %2
                               (str prefix b)
                               (if (< (+ % 1) (count children)) "|-- " "\\-- ")
                               (if (< (+ % 1) (count children)) "|   " "    ")))
                (clojure.string/join)))))

  (rec-format-to-string root-name "" "" ""))

(defn load-children [content r]
  (->> content
       (re-seq r)
       (mapv #(str (get % 1) "Scope"))))

(defn find-scopes [path-to-proj file-reg child-regs]
  (->> (java.io.File. path-to-proj)
       (file-seq)
       (filter #(re-matches file-reg (.getName %)))
       (mapv (fn [x]
               (let [content (slurp x)]
                 {:name (second (re-find #"@\w*?\.?(\w+Scope)" content))
                  :children-names (mapcat #(load-children content %) child-regs)})))))

(defn get-scope-files [path-to-proj]
  (->> (java.io.File. path-to-proj)
       (file-seq)
       (filter #(re-matches #".+\.(kt|java)" (.getName %)))
       (remove #(re-matches #".+(Di|Component|Module).(kt|java)" (.getName %)))
       (mapv #(let [content (slurp %)]
                {:scope (second (re-find #"@(\w+Scope)" content))
                 :filename (second (re-find #"(.+)\.[ktjava]+" (.getName %)))}))
       (filter #(:scope %))
       (vec)))

(defn -main [& args]
  (let [path-to-proj (first args)]
    (->>
     (concat
      (find-scopes path-to-proj #".+Di\.kt" [#"get(.+?)Component" #"Component: (\w+)Component"])
      (find-scopes path-to-proj #".+Component\.java" [#"(\w+)Component \w+Component\(" #"(\w+)Component.Builder get\w+Builder()"]))
     (format-to-string "AppScope" (get-scope-files path-to-proj))
     (println))))
