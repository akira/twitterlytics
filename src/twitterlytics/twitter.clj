(ns twitterlytics.twitter
  (:use
    [twitter.oauth]
    [twitter.api.restful])
  (:require
    [clojure.set :as set]
    [clojure.string :as str])
  (:import
    java.text.SimpleDateFormat))

(def creds-config (read-string (slurp "config.clj")))

(def my-creds (make-oauth-creds
                (:app_consumer_key creds-config)
                (:app_consumer_secret creds-config)
                (:user_access_token creds-config)
                (:user_access_token_secret creds-config)))

(def date-parser (SimpleDateFormat. "EEE MMM dd HH:mm:ss ZZZZZ yyyy"))

(defn parse-date-str [date-str]
  (if date-str (.parse date-parser date-str)))

(defn sort-by-date [users]
  (sort-by #(-> % :status :created_at parse-date-str) users))

(defn mapped-user [user]
  select-keys user [:name :screen_name :status :profile_image_url])

(defn with-creds [f params]
  (f :oauth-creds my-creds :params params))

(defn twitter-users-lookup [part-ids]
  (with-creds users-lookup {:user_id (str/join "," part-ids)}))

(defn twitter-following-ids [screen-name]
  (-> (with-creds friends-ids {:screen_name screen-name}) :body :ids))

(defn twitter-follower-ids [screen-name]
  (-> (with-creds followers-ids {:screen_name screen-name}) :body :ids))

(defn users-from-ids [id-list]
  (let [part-ids (partition-all 100 id-list)]
    (flatten (concat
      (pmap (comp :body twitter-users-lookup) part-ids)))))


(defn cached-fn [filename fn & args]
  ( if (.exists (clojure.java.io/as-file filename))
    (read-string (slurp filename))
    (let [result (apply fn args)
          _ (spit filename result)]
      result)))

(defn user-stats [screen-name]
  (let [friends (set (twitter-following-ids screen-name))
        followers (set (twitter-follower-ids screen-name))
        are-following (set/intersection friends followers)
        not-following (set/difference friends followers)]
    {:user          (-> (with-creds users-show {:screen_name screen-name}) :body),
     :are-following (sort-by-date (users-from-ids are-following)),
     :not-following (sort-by-date (users-from-ids not-following))}))
