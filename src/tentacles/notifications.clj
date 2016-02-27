(ns tentacles.notifications
  "Implements ght Github Notification API: https://developer.github.com/v3/activity/notifications/"
  (:use [tentacles.core :only [api-call]]))

(defn my-notifications
  "List all notifications for the current user, grouped by repository.
   Options are:
   all            -- show notifications marked as read. Defaults to false.
   participating  -- shows notifications in which the user is directly participating or mentioned. Defaults to false.
   since          -- string ISO 8601 timestamp."
  [& [options]]
  (api-call :get "notifications" nil options))

(defn repo-notifications
  "List all notifications for the current user and repository.
  Options are:
   all            -- show notifications marked as read. Defaults to false.
   participating  -- show notifications in which the user is directly participating or mentioned. Defaults to false.
   since          -- string ISO 8601 timestamp."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/notifications" [user repo] options))
