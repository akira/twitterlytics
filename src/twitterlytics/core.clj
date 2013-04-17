(ns twitterlytics.core
  (:use [compojure.core :only (defroutes GET POST)]
        [ring.adapter.jetty :as ring]
        [ring.middleware.params :only [wrap-params]]
        [hiccup.page :only (html5)]
        [hiccup.bootstrap.page]
        [hiccup.bootstrap.middleware]
        [twitterlytics.twitter :as twitter]))

(def project-name "Twitterlizer")

(defn html-user-row [user]
  [:li
    [:div.row
      [:div.span1 (if (:profile_image_url user) [:img {:src (:profile_image_url user) :style "width: 48px; height: 48px;"}])]
      [:div.span6
        [:span (:name user) "&nbsp;"]
        [:a {:href (str "http://twitter.com/" (:screen_name user)), :target "_blank"} (str "@" (:screen_name user))]
        [:p (-> user :status :created_at) ]]]
    [:hr ]])

(defn html-user-list [users]
  (map html-user-row users))

(defn user-header [user]
  [:div.row-fluid
    [:div.span12 {:style "padding-left: 25px"}
      [:div.span1
        (if (:profile_image_url user) [:img {:src (:profile_image_url user)}])]
      [:div.span8
        [:h1(:name user) "&nbsp;"]
        [:h3 [:a {:href (str "http://twitter.com/" (:screen_name user)), :target "_blank"}
          (str "@" (:screen_name user))]]]]
    [:div.span4 {:style "padding-top: 10px"}
      [:table.table.table-bordered
        [:tr
          [:td (:statuses_count user)]
          [:td (:friends_count user)
          [:td (:followers_count user)]]]
        [:tr
          [:td "Tweets"]
          [:td "Following"]
          [:td "Followers"]]]]])

(defn bootstrap-layout [content]
  (html5
    [:head
      [:title "Twitter Follower Analysis"]
      [:script {:src "http://code.jquery.com/jquery-latest.js"}]
      (include-bootstrap)]
    [:body
      [:div.navbar.navbar-inverse.navbar-fixed-top
        [:div.navbar-inner
          [:div.container
           [:a.btn.btn-navbar {:data-toggle "collapse", :data-target ".nav-collapse"}
             [:span.icon-bar]]
           [:a.brand {:href "/"} project-name]
           [:div.nav-collapse.collapse
             [:ul.nav
               [:li.active
                 [:a {:href "/"} "Home"]]]]]]]
     [:div.container
       [:br]
       [:br]
       [:br]
       (:body content)]]))

(defn show-stats [screen-name]
  (let [stats (twitter/user-stats screen-name)]
    (bootstrap-layout {
      :body
      [:div
        [:br]
        (user-header (stats :user))
        [:div.tabbable
          [:ul.nav.nav-tabs
            [:li.active [:a {:href "#tab1", :data-toggle "tab"} "Not Following (" (count (stats :not-following)) ")"]]
            [:li [:a {:href "#tab2", :data-toggle "tab"} "Following (" (count (stats :are-following)) ")"]]]
          [:div.tab-content
           [:div.tab-pane.active {:id "tab1" :style "border: 1px solid #ddd; padding: 10px;"}
             [:p [:ul.unstyled (html-user-list (stats :not-following))]]]
           [:div.tab-pane {:id "tab2" :style "border: 1px solid #ddd; padding: 10px;"}
             [:p [:ul.unstyled (html-user-list (stats :are-following))]]]]]]})))

(defn show-home []
  (bootstrap-layout {
    :body
    [:div
      [:h1 (str "Welcome to " project-name)]
      [:br]
      [:div.hero-unit
        [:p
          (str
            project-name " is a personal analysis tool for twitter.  "
            "Figure out who to unfollow based on your stats.")]
        [:p ]
        [:p "Analyze your followers, who is also following you, inactive users, time of their last tweets, and more."]
        [:p {:style "padding-top: 5px"}
          [:h2 "Enter Twitter username:"]
          [:form {:action "/stats", :method "post"}
            [:input {:type "input", :name "username"}]
            "&nbsp;&nbsp;"
            [:input.btn.btn-primary.btn-medium {:value "Analyze", :type "submit"}]]]]]}))

(defroutes routes
  (POST "/stats" [username] (show-stats username))
  (GET "/" [] (show-home)))

(def handler
  (wrap-params (wrap-bootstrap-resources routes)))

(defn -main []

  (run-jetty routes {:port 8080 :join? false}))
