(ns tentacles.gists
  "Implements the Github Gists API: http://developer.github.com/v3/gists/"
  (:use [tentacles.core :only [api-call]]
        [slingshot.slingshot :only [try+]]))

;; ## Primary gist API

(defn user-gists
  "List a user's gists."
  [user & [options]]
  (api-call :get "users/%s/gists" [user] options))

(defn gists
  "If authenticated, list the authenticated user's gists. Otherwise,
   return all public gists."
  [& [options]]
  (api-call :get "gists" nil options))

(defn public-gists
  "List all public gists."
  []
  (api-call :get "gists/public" nil nil))

(defn starred-gists
  "List the authenticated user's starred gists."
  [options]
  (api-call :get "gists/starred" nil options))

(defn specific-gist
  "Get a specific gist."
  [id & [options]]
  (api-call :get "gists/%s" [id] options))

;; For whatever insane reason, Github expects gist files to be passed
;; as a JSON hash of filename -> hash of content -> contents rather than
;; just filename -> contents. I'm not going to be a dick and require that
;; users of this library pass maps like that.
;;
;; It *does* make sense in edit-gist, however, since we can selectively update
;; a file's name and/or content, or both. I imagine that Github chose to require
;; a subhash with a content key in the creation api end-point for consistency.
;; In our case, I think I'd rather have a sensible gist creation function.
(defn- file-map [options files]
  (assoc options
    :files (into {} (for [[k v] files] [k {:content v}]))))

(defn create-gist
  "Create a gist. files is a map of filenames to contents.
   Options are:
      description -- A string description of the gist.
      public      -- true (default) or false; whether or not the gist is public."
  [files & [options]]
  (api-call :post "gists" nil
            (assoc (file-map options files)
              :public (:public options true))))

;; It makes sense to require the user to pass :files the way Github expects it
;; here: as a map of filenames to maps of :contents and/or :filename. It makes
;; sense because users can selectively update only certain parts of a gist. A
;; map is a clean way to express this update.
(defn edit-gist
  "Edit a gist.
   Options are:
      description -- A string to update the description to.
      files       -- A map of filenames to maps. These submaps may
                     contain either of the following, or both: a
                     :contents key that will replace the gist's
                     contents, and a :filename key that will replace
                     the name of the file. If one of the file keys in
                     the map is associated with 'nil', it'll be deleted."
  [id & [options]]
  (api-call :post "gists/%s" [id] options))

(defn star-gist
  "Star a gist."
  [id & [options]]
  (nil? (api-call :put "gists/%s/star" [id] options)))

(defn unstar-gist
  "Unstar a gist."
  [id & [options]]
  (nil? (api-call :delete "gists/%s/star" [id] options)))

;; Github sends 404 which clj-http throws an exception for if a gist
;; is not starred. I'd rather get back true or false.
(defn starred?
  "Check if a gist is starred."
  [id & [options]]
  (try+
   (nil? (api-call :get "gists/%s/star" [id] options))
   (catch [:status 404] _ false)))

(defn fork-gist
  "Fork a gist."
  [id & [options]]
  (api-call :post "gists/%s/fork" [id] options))

(defn delete-gist
  "Delete a gist."
  [id & [options]]
  (nil? (api-call :delete "gists/%s" [id] options)))

;; ## Gist Comments API

(defn comments
  "List comments for a gist."
  [id & [options]]
  (api-call :get "gists/%s/comments" [id] options))

(defn specific-comment
  "Get a specific comment."
  [comment-id & [options]]
  (api-call :get "gists/comments/%s" [comment-id] options))

(defn create-comment
  "Create a comment."
  [id body options]
  (api-call :post "gists/%s/comments" [id] (assoc options :body body)))

(defn edit-comment
  "Edit a comment."
  [comment-id body options]
  (api-call :post "gists/comments/%s" [comment-id] (assoc options :body body)))

(defn delete-comment
  "Delete a comment."
  [comment-id options]
  (nil? (api-call :delete "gists/comments/%s" [comment-id] options)))