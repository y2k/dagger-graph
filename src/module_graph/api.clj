(ns module-graph.api
  (:import (org.jsoup Jsoup))
  (:require [clojure.string :as s]))

(->>
 (parseDocument (slurp api-file))
 (map
  (fn [e]
    (defn parseClassName [e]
      (.text (.selectFirst e "h2.panel-title")))
    (defn parseProperties [e]
      (defn findLastTab [e] (.select e "table.table.table-striped"))
      (defn parseProperty [e]
        {:name (.text (.selectFirst e "td"))
         :type (.text (second (.select e "td")))})
      (let [last-tab (findLastTab e)]
        (->> (.select last-tab "tbody > tr")
             (mapv parseProperty))))

    {:class (parseClassName e)
     :props (parseProperties e)}))
 (map
  (fn [x]
    (defn fix-prop [p]
      (assoc p
             :fixed-name (s/replace (:name p) #"_(\w)" #(s/upper-case (% 1)))
             :type (case (:type p)
                     "bool" "Bool"
                     "int" "Long"
                     "string[]" "List<String>"
                     "int[]" "List<Long>"
                     "string" "String"
                     "float" "Float"
                     "float64" "Double"
                     "array" "List<*>"
                     (:type p))))
    {:class (:class x)
     :props (map fix-prop (:props x))}))
 (map
  (fn [x]
    (defn format-props [props]
      (->> props
           (map #(str "\t@SerializedName(\"" (:name %) "\")\n\tval " (:fixed-name %) ": " (:type %)))
           (s/join ",\n\n")))
    (str "class " (:class x) "(\n" (format-props (:props x)) "\n)\n")))
 (s/join "\n")
 ((fn [x] (spit (str (System/getProperty "user.home") "/result.kt") x) nil)))
