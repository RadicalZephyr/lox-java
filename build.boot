(set-env!
 :resource-paths #{"resources"}
 :source-paths #{"src"}
 :dependencies '[[radicalzephyr/boot-junit "0.4.0"  :scope "test"]
                 [junit                    "4.12"   :scope "test"]])

(def +version+ "0.1.0-SNAPSHOT")

(require '[radicalzephyr.bootlaces  :refer :all]
         '[radicalzephyr.boot-junit :refer [junit]])

(set-env! :resource-paths #{}) ;; Must undo bootlaces! adding "src" to
                               ;; the resource-paths. Will be fixed in 0.1.15

(task-options!
 pom {:project 'radicalzephyr/lox-lang
      :version +version+
      :description "An interpreter for the Lox language."
      :url "https://github.com/RadicalZephyr/lox-lang-java"
      :scm {:url "https://github.com/RadicalZephyr/lox-lang-java.git"}
      :licens {"MIT" "http://opensource.org/licenses/MIT"}}
 sift {:invert true
       :include #{#"\.java$"}}
 junit {:class-names #{"UnitTests"}})

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(if ((loaded-libs) 'boot.user)
  (ns-unmap 'boot.user 'test))

(deftask test
  "Compile and run my jUnit unit tests."
  []
  (comp (javac)
        (junit)))

(deftask build
  "Build the Lox interpreter."
  []
  (comp (javac)
        (pom)
        (sift)
        (jar)))

(deftask dev
  "Setup a recurring, notifying development build"
  []
  (comp (watch)
        (notify :audible true)
        (test)))

(deftask run
  "Run the Lox interpreter."
  [a args ARG [str] "the arguments for the application."]
  (comp (javac)
        (with-pass-thru _
          (eval `(radicalzephyr.lox.Lox/main (into-array String ~args))))))

(def lox-expression-ast
  {"Binary"    ["Expr" "left" "Token" "operator" "Expr" "right"]
   "Grouping"  ["Expr" "expression"]
   "Literal"   ["Object" "value"]
   "Unary"     ["Token" "operator" "Expr" "right"]
   "Ternary"   ["Token" "operator" "Expr" "test" "Expr" "then" "Expr" "else_"]})

(def lox-statement-ast
  {"Expression" ["Expr" "expression"]
   "Print"      ["Expr" "expression"]})

(defn write-ast [target-dir base-name ast-types]
  (if-let [gen-fn (resolve 'radicalzephyr.tool.generate-ast/generate-ast)]
    (do
      (gen-fn target-dir base-name ast-types)
      (boot.util/info "Successfully wrote AST data structures into %s%s.java\n" target-dir base-name))
    (boot.util/fail "Could not find generate-ast function.")))

(deftask gen-ast
  "Generate the AST classes."
  []
  (with-pass-thru _
    (require 'radicalzephyr.tool.generate-ast)
    (write-ast "src/radicalzephyr/lox/" "Expr" lox-expression-ast)
    (write-ast "src/radicalzephyr/lox/" "Stmt" lox-statement-ast)))
