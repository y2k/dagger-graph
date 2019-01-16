(ns module-graph.api
  (:import (org.jsoup Jsoup)))

(defn parseDocument [html]
  (defn f [xs] (map #(-> % (.parent) (.parent)) xs))
  (-> (Jsoup/parse html)
      (.select "h2[id^=model_]")
      (f)))

(defn toItem2 [element] (str element))

(defn getParams [element]
  (let [table (.selectFirst element "tbody")]
    (if (nil? table) nil
        (->>
         (.select table "tr")
         (distinctby #(-> % (.child 0) (.text)))
         (map toItem2)
         (clojure.string/join "")))))

(defn distinctby [f col]
  (->> col
       (group-by f)
       (map (comp first second))))

(defn toItem [element]
  (let [className (toClassName element)
        params (getParams element)]
    (if (nil? params)
      nil
      (str "class "  className  "(\n" params + "\n)"))))

(defn toClassName [element]
  (defn getPath [e] (-> (.selectFirst e "h2.panel-title") (.text)))
  (defn f [x] (clojure.string/replace x #"-(\w)" #(.toUpperCase (%1 1))))
  (->> (.split (getPath element) "/")
       (last)
       f))

(comment

  (count (parseDocument (slurp api-file)))
  (def element (first (parseDocument (slurp api-file))))
  (.text element)
  (getParams element)

  (type table)
  (def table (.selectFirst element "tbody"))

  (def xs (vec (.select table "tr")))
  (distinctby #(-> % (.child 0) (.text)) xs)
  (-> (first xs) (.child 0))
  ; (.child (first xs) 0)

  (let [table (.selectFirst element "tbody")]
    (if (nil? table) nil
        (->>
         (.select table "tr")
         (distinctby #(-> % (.child 0) (.text)))
         (map toItem2)
         (clojure.string/join ""))))

  ; (def path (getPath element))
  ; (toClassName (getPath element))
  ; (toItem element)

  (->>
   (slurp api-file)
   (parseDocument)
   (map toItem)
   (first)
   (println))

  (comment))
