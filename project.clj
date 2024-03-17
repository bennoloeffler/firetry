(defproject firetry "0.1.0-SNAPSHOT"

  :description "bels try to use firebase in cljs app"
  :url "to yet"

  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"

  :dependencies
  [[org.clojure/clojurescript "1.11.60" :scope "provided"]
   [com.google.javascript/closure-compiler-unshaded "v20220803"]
   [thheller/shadow-cljs "2.20.3" :scope "provided"]
   [reagent "1.1.1"]
   [re-frame "1.4.2"]
   [bidi "2.1.6"]
   [clj-commons/pushy "0.3.10"]
   [re-pressed "0.3.2"]
   [breaking-point "0.1.2"]
   [binaryage/devtools "1.0.6"]
   [re-frisk "1.6.0"]
   [day8/shadow-git-inject "0.0.5"]
   [funcool/promesa "11.0.678"]
   [cljs-bean "1.9.0"]
   [camel-snake-kebab "0.4.3"]])
  