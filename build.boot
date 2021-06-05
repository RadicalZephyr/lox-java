(set-env!
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
        (notify :visual true)
        (test)))
