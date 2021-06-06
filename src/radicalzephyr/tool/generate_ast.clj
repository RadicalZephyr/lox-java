(ns radicalzephyr.tool.generate-ast
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def ^:dynamic *indent* 0)

(defmacro with-indent [increase & body]
  `(binding [*indent* (+ ~increase *indent*)]
     ~@body))

(defmacro with-nested-brackets [& body]
  `(do
     ;; append to the current line with no nesting
     (println " {")
     (with-indent 2
       ~@body)
     (println-nested "}")))

(defn indent-spaces []
  (apply str (repeat *indent* " ")))

(defn println-nested [& args]
  (if (seq args)
    (apply println (indent-spaces) args)
    (println)))

(defn printf-nested [format & args]
  (apply printf (str (indent-spaces) format) args))

(defn field-names [fields]
  (->> fields
       (drop 1)
       (take-nth 2)))

(defn- as-param-list
  [fields]
  (->> fields
       (partition 2)
       (map #(str (first %) " " (second %)))
       (str/join ", ")))

(defn generate-type [base-name type-name fields]
  (printf-nested "static class %s extends %s" type-name base-name)
  (with-nested-brackets
    ;; Fields
    (doseq [[type-name field-name] (partition 2 fields)]
      (printf-nested "final %s %s;\n" type-name field-name))
    (println-nested)

    ;; Constructor
    (printf-nested "%s(%s)" type-name (as-param-list fields))
    (with-nested-brackets
      (doseq [field-name (field-names fields)]
        (printf-nested "this.%1$s = %1$s;\n" field-name)))))


(defn generate-ast [output-dir base-name types]
  (let [file-name (io/file output-dir (str base-name ".java"))]
    (io/make-parents file-name)
   (with-open [ast-file (io/writer file-name)]
     (binding [*out* ast-file]
       (println "package radicalzephyr.lox;")
       (println)
       (println "import java.util.List;")
       (println)
       (printf "abstract class %s" base-name)
       (with-nested-brackets
         (doseq [[type-name fields] types]
           (generate-type base-name type-name fields)
           (println-nested)))))))
