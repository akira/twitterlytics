(defproject twitterlytics "0.1.0-SNAPSHOT"
  :description "twitterlytics analyzer"
  :url "http://alexkira.com"
  :main twitterlytics.core
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler twitterlytics.core/handler}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.2"]
                 [twitter-api "0.7.0"]
                 [hiccup-bootstrap "0.1.1"]])
