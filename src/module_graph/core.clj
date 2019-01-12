(ns module-graph.core)

(defn format-to-string [root-name modules]
  (defn get-children-modules [name all-modules]
    (->> all-modules
         (filter #(= name (:name %)))
         (first)
         (:children-names)))

  (defn rec-format-to-string [module-name prefix a b]
    (str prefix a module-name "\n"
         (let [children (get-children-modules module-name modules)]
           (->> children
                (map-indexed #(addToStringBuilder
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

(->>
 (concat
  (->> (java.io.File. path-to-proj)
       (file-seq)
       (filter #(re-matches #".+Di\.kt" (.getName %)))
       (mapv (fn [x]
               (let [content (slurp x)]
                 {:name (get (re-find #"@\w*?\.?(\w+Scope)" content) 1)
                  :children-names (vec (concat
                                        (load-children content #"get(.+?)Component")
                                        (load-children content #"Component: (\w+)Component")))}))))
  (->> (java.io.File. path-to-proj)
       (file-seq)
       (filter #(re-matches #".+Component\.java" (.getName %)))
       (mapv (fn [x]
               (let [content (slurp x)]
                 {:name (get (re-find #"@\w*?\.?(\w+Scope)" content) 1)
                  :children-names (vec (concat
                                        (load-children content #"(\w+)Component \w+Component\(")
                                        (load-children content #"(\w+)Component.Builder get\w+Builder()")))})))))
 (format-to-string "AppScope")
 (println))
