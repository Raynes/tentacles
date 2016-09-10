(ns tentacles.repos
  "Implements the Github Repos API: http://developer.github.com/v3/repos/"
  (:refer-clojure :exclude [keys])
  (:require [clojure.data.codec.base64 :as b64])
  (:use [clj-http.client :only [post put]]
        [clojure.java.io :only [file]]
        [tentacles.core :only [api-call no-content? raw-api-call]]
        [cheshire.core :only [generate-string]]))

;; ## Primary Repos API

(defn all-repos
  "Lists all of the repositories, in the order they were created.
   Options are:
      since -- integer ID of the last repository seen."
  [& [options]]
  (api-call :get "repositories" nil options))

(defn repos
  "List the authenticated user's repositories.
   Options are:
      type -- all (default), public, private, member."
  [options]
  (api-call :get "user/repos" nil options))

(defn user-repos
  "List a user's repositories.
   Options are:
      types -- all (default), public, private, member."
  [user & [options]]
  (api-call :get "users/%s/repos" [user] options))

(defn org-repos
  "List repositories for an organization.
   Options are:
      type -- all (default), public, private."
  [org & [options]]
  (api-call :get "orgs/%s/repos" [org] options))

(defn create-repo
  "Create a new repository.
   Options are:
      description   -- Repository's description.
      homepage      -- Link to repository's homepage.
      public        -- true (default), false.
      has-issues    -- true (default), false.
      has-wiki      -- true (default), false.
      has-downloads -- true (default), false."
  [name options]
  (api-call :post "user/repos" nil (assoc options :name name)))

(defn create-org-repo
  "Create a new repository in an organization..
   Options are:
      description   -- Repository's description.
      homepage      -- Link to repository's homepage.
      public        -- true (default), false.
      has-issues    -- true (default), false.
      has-wiki      -- true (default), false.
      has-downloads -- true (default), false.
      team-id       -- Team that will be granted access to this
                       repository."
  [org name options]
  (api-call :post "orgs/%s/repos" [org] (assoc options :name name)))

(defn specific-repo
  "Get a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s" [user repo] options))

(defn edit-repo
  "Edit a repository.
   Options are:
      description   -- Repository's description.
      name          -- Repository's name.
      homepage      -- Link to repository's homepage.
      public        -- true, false.
      has-issues    -- true, false.
      has-wiki      -- true, false.
      has-downloads -- true, false."
  [user repo options]
  (api-call :patch "repos/%s/%s"
            [user repo]
            (if (:name options)
              options
              (assoc options :name repo))))

(defn contributors
  "List the contributors for a project.
   Options are:
      anon -- true, false (default): If true, include
              anonymous contributors."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/contributors" [user repo] options))

(defn languages
  "List the languages that a repository uses."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/languages" [user repo] options))

(defn teams
  "List a repository's teams."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/teams" [user repo] options))

(defn tags
  "List a repository's tags."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/tags" [user repo] options))

(defn branches
  "List a repository's branches."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/branches" [user repo] options))

;; ## Repo Collaborators API

(defn collaborators
  "List a repository's collaborators."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/collaborators" [user repo] options))

(defn collaborator?
  "Check if a user is a collaborator."
  [user repo collaborator & [options]]
  (no-content? (api-call :get "repos/%s/%s/collaborators/%s" [user repo collaborator] options)))

(defn add-collaborator
  "Add a collaborator to a repository."
  [user repo collaborator options]
  (no-content? (api-call :put "repos/%s/%s/collaborators/%s" [user repo collaborator] options)))

(defn remove-collaborator
  "Remove a collaborator from a repository."
  [user repo collaborator options]
  (no-content? (api-call :delete "repos/%s/%s/collaborators/%s" [user repo collaborator] options)))

;; ## Repo Commits API

(defn commits
  "List commits for a repository.
   Options are:
      sha  -- Sha or branch to start lising commits from.
      path -- Only commits at this path will be returned."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/commits" [user repo] options))

(defn specific-commit
  "Get a specific commit."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/commits/%s" [user repo sha] options))

(defn commit-comments
  "List the commit comments for a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/comments" [user repo] options))

(defn specific-commit-comments
  "Get the comments on a specific commit."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/commits/%s/comments" [user repo sha] options))

;; 'line' is supposed to be a required argument for this API call, but
;; I'm convinced that it doesn't do anything. The only thing that seems
;; to matter is the 'position' argument. As a matter of fact, we can omit
;; 'line' entirely and Github does not complain, despite it supposedly being
;; a required argument.
;;
;; Furthermore, it requires that the sha be passed in the URL *and* the JSON
;; input. I don't see how they can ever possibly be different, so we're going
;; to just require one sha.
(defn create-commit-comment
  "Create a commit comment. path is the location of the file you're commenting on.
   position is the index of the line you're commenting on. Not the actual line number,
   but the nth line shown in the diff."
  [user repo sha path position body options]
  (api-call :post "repos/%s/%s/commits/%s/comments" [user repo sha]
            (assoc options
              :body body
              :commit-id sha
              :path path
              :position position)))

(defn specific-commit-comment
  "Get a specific commit comment."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/comments/%s" [user repo id] options))

(defn update-commit-comment
  "Update a commit comment."
  [user repo id body options]
  (api-call :post "repos/%s/%s/comments/%s" [user repo id] (assoc options :body body)))

(defn compare-commits
  [user repo base head & [options]]
  (api-call :get "repos/%s/%s/compare/%s...%s" [user repo base head] options))

(defn delete-commit-comment
  [user repo id options]
  (no-content? (api-call :delete "repos/%s/%s/comments/%s" [user repo id] options)))

;; ## Repo Downloads API

(defn downloads
  "List the downloads for a repository."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/downloads" [user repo] options))

(defn specific-download
  "Get a specific download."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/downloads/%s" [user repo id] options))

(defn delete-download
  "Delete a download"
  [user repo id options]
  (no-content? (api-call :delete "repos/%s/%s/downloads/%s" [user repo id] options)))

;; Github uploads are a two step process. First we get a download resource and then
;; we use that to upload the file.
(defn download-resource
  "Get a download resource for a file you want to upload. You can pass it
   to upload-file to actually upload your file."
  [user repo path options]
  (let [path (file path)]
    (assoc (api-call :post "repos/%s/%s/downloads"
                     [user repo]
                     (assoc options
                       :name (.getName path)
                       :size (.length path)))
      :filepath path)))

;; This isn't really even a Github API call, since it calls an Amazon API.
;; As such, it doesn't provide the same guarentees as the rest of the API.
;; We'll just return the raw response.
(defn upload-file
  "Upload a file given a download resource obtained from download-resource."
  [resp]
  (post (:s3_url resp)
        {:multipart [["key" (:path resp)]
                     ["acl" (:acl resp)]
                     ["success_action_status" "201"]
                     ["Filename" (:name resp)]
                     ["AWSAccessKeyId" (:accesskeyid resp)]
                     ["Policy" (:policy resp)]
                     ["Signature" (:signature resp)]
                     ["Content-Type" (:mime_type resp)]
                     ["file" (:filepath resp)]]}))

;; Repo Forks API

(defn forks
  "Get a list of a repository's forks."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/forks" [user repo] options))

(defn create-fork
  "Create a new fork.
   Options are:
      org -- If present, the repo is forked to this organization."
  [user repo options]
  (api-call :post "repos/%s/%s/forks" [user repo] options))

;; Repo Deploy Keys API

(defn keys
  "List deploy keys for a repo."
  [user repo options]
  (api-call :get "repos/%s/%s/keys" [user repo] options))

(defn specific-key
  "Get a specific deploy key."
  [user repo id options]
  (api-call :get "repos/%s/%s/keys/%s" [user repo id] options))

(defn create-key
  "Create a new deploy key."
  [user repo title key options]
  (api-call :post "repos/%s/%s/keys" [user repo]
            (assoc options :title title :key key)))

(defn delete-key
  "Delete a deploy key."
  [user repo id options]
  (api-call :delete "repos/%s/%s/keys/%s" [user repo id] options))

;; Repo Watcher API

(defn watchers
  "List a repository's watchers."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/watchers" [user repo] options))

(defn watching
  "List all the repositories that a user is watching."
  [user & [options]]
  (api-call :get "users/%s/watched" [user] options))

(defn watching?
  "Check if you are watching a repository."
  [user repo options]
  (no-content? (api-call :get "user/watched/%s/%s" [user repo] options)))

(defn watch
  "Watch a repository."
  [user repo options]
  (no-content? (api-call :put "user/watched/%s/%s" [user repo] options)))

(defn unwatch
  "Unwatch a repository."
  [user repo options]
  (no-content? (api-call :delete "user/watched/%s/%s" [user repo] options)))

;; ## Repo Stargazers

(defn stargazers
  "List a repository's stargazers."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/stargazers" [user repo] options))

(defn starring
  "List all the repositories that a user is starring."
  [user & [options]]
  (api-call :get "users/%s/starred" [user] options))

(defn starring?
  "Check if you are watching a repository."
  [user repo options]
  (no-content? (api-call :get "user/starred/%s/%s" [user repo] options)))

(defn star
  "Star a repository."
  [user repo options]
  (no-content? (api-call :put "user/starred/%s/%s" [user repo] options)))

(defn unstar
  "Unstar a repository"
  [user repo options]
  (no-content? (api-call :delete "user/starred/%s/%s" [user repo] options)))

;; ## Repo Hooks API

(defn hooks
  "List the hooks on a repository."
  [user repo options]
  (api-call :get "repos/%s/%s/hooks" [user repo] options))

(defn specific-hook
  "Get a specific hook."
  [user repo id options]
  (api-call :get "repos/%s/%s/hooks/%s" [user repo id] options))

(defn create-hook
  "Create a hook.
   Options are:
      events -- A sequence of event strings. Only 'push' by default.
      active -- true or false; determines if the hook is actually triggered
                on pushes."
  [user repo name config options]
  (api-call :post "repos/%s/%s/hooks" [user repo name config]
            (assoc options
              :name name, :config config)))

(defn edit-hook
  "Edit an existing hook.
   Options are:
      name          -- Name of the hook.
      config        -- Modified config.
      events        -- A sequence of event strings. Replaces the events.
      add_events    -- A sequence of event strings to be added.
      remove_events -- A sequence of event strings to remove.
      active        -- true or false; determines if the hook is actually
                       triggered on pushes."
  [user repo id options]
  (api-call :patch "repos/%s/%s/hooks/%s" [user repo id] options))

(defn test-hook
  "Test a hook."
  [user repo id options]
  (no-content? (api-call :post "repos/%s/%s/hooks/%s/test" [user repo id] options)))

(defn delete-hook
  "Delete a hook."
  [user repo id options]
  (no-content? (api-call :delete "repos/%s/%s/hooks/%s" [user repo id] options)))

;; ## PubSubHubbub

(defn pubsubhubub
  "Create or modify a pubsubhubub subscription.
   Options are:
      secret -- A shared secret key that generates an SHA HMAC of the
                payload content."
  [user repo mode event callback & [options]]
  (no-content?
   (post "https://api.github.com/hub"
         (merge
           (when-let [oauth-token (:oauth-token options)]
             {:headers {"Authorization" (str "token " oauth-token)}})
           {:basic-auth (:auth options)
            :form-params
            (merge
             {"hub.mode" mode
              "hub.topic" (format "https://github.com/%s/%s/events/%s"
                                  user repo event)
              "hub.callback" callback}
             (when-let [secret (:secret options)]
               {"hub.secret" secret}))}))))

;; ## Repo Contents API

(defn- decode-b64
  "Decodes a base64 encoded string in a response"
  ([res str? path]
     (if (and (map? res) (= (:encoding res) "base64"))
       (if-let [^String encoded (get-in res path)]
         (if (not (empty? encoded))
           (let [trimmed (.replace encoded "\n" "")
                 raw (.getBytes trimmed "UTF-8")
                 decoded (if (seq raw) (b64/decode raw) (byte-array))
                 done (if str? (String. decoded "UTF-8") decoded)]
             (assoc-in res path done))
           res)
         res)
       res))
  ([res str?] (decode-b64 res str? [:content]))
  ([res] (decode-b64 res false [:content])))

(defn encode-b64 [content]
  (String. (b64/encode (.getBytes content "UTF-8")) "UTF-8"))

(defn readme
  "Get the preferred README for a repository.
   Options are:
      ref  -- The name of the Commit/Branch/Tag. Defaults to master.
      str? -- Whether the content should be decoded to String. Defaults to true."
  [user repo {:keys [str?] :or {str? true} :as options}]
  (decode-b64
   (api-call :get "repos/%s/%s/readme" [user repo] (dissoc options :str?))
   str?))

(defn contents
  "Get the contents of any file or directory in a repository.
   Options are:
      ref  -- The name of the Commit/Branch/Tag. Defaults to master.
      str? -- Whether the content should be decoded to a String. Defaults to false (ByteArray)."
  [user repo path {:keys [str?] :as options}]
  (decode-b64
   (api-call :get "repos/%s/%s/contents/%s" [user repo path] (dissoc options :str?))
   str?))

(defn update-contents
  "Update a file in a repository
   path -- The content path.
   message -- The commit message.
   content -- The updated file content, Base64 encoded.
   sha -- The blob SHA of the file being replaced.
   Options are:
      branch    -- The branch name. Default: the repository’s default branch (usually master)
      author    -- A map containing :name and :email for the author of the commit
      committer -- A map containing :name and :email for the committer of the commit"
  [user repo path message content sha & [options]]
  (let [body (merge {:message message
                     :content (encode-b64 content)
                     :sha     sha}
                    options)]
    (api-call :put "repos/%s/%s/contents/%s" [user repo path] body)))

(defn delete-contents
  "Delete a file in a repository
   path    -- The content path.
   message -- The commit message.
   sha     -- The blob SHA of the file being deleted.
   Options are:
      branch    -- The branch name. Default: the repository’s default branch (usually master)
      author    -- A map containing :name and :email for the author of the commit
      committer -- A map containing :name and :email for the committer of the commit"
  [user repo path message sha & [options]]
  (let [body (merge {:message message
                     :sha     sha}
                    options)]
    (api-call :delete "repos/%s/%s/contents/%s" [user repo path] body)))

(defn archive-link
  "Get a URL to download a tarball or zipball archive for a repository.
   Options are:
      ref -- The name of the Commit/Branch/Tag. Defaults to master."
  ([user repo archive-format {git-ref :ref :or {git-ref ""} :as options}]
     (let [proper-options (-> options
                              (assoc :follow-redirects false)
                              (dissoc :ref))
           resp (raw-api-call :get "repos/%s/%s/%s/%s" [user repo archive-format git-ref] proper-options)]
       (if (= (resp :status) 302)
         (get-in resp [:headers "location"])
         resp))))

;; ## Status API
(def combined-state-opt-in "application/vnd.github.she-hulk-preview+json")

(defn statuses
  "Returns the combined status of a ref (SHA, branch, or tag).
  By default, returns the combined status. Include `:combined? false'
  in options to disable combined status
  (see https://developer.github.com/v3/repos/statuses/#get-the-combined-status-for-a-specific-ref)"
  [user repo ref & [options]]
  (let [combined? (:combined? options true)]
    (api-call :get
              (if combined?
                "repos/%s/%s/commits/%s/status"
                "repos/%s/%s/statuses/%s")
              [user repo ref]
              (cond-> options
                      combined? (assoc :accept combined-state-opt-in)))))

(defn create-status
  "Creates a status.
  Options are: state target-url description context; state is mandatory"
  [user repo sha options]
  (api-call :post "repos/%s/%s/statuses/%s" [user repo sha]
            (assoc options
              :accept combined-state-opt-in)))

;; ## Deployments API
(def deployments-opt-in "application/vnd.github.cannonball-preview+json")

(defn deployments
  "Returns deployments for a repo"
  [user repo & [options]]
  (api-call :get "repos/%s/%s/deployments" [user repo]
            (assoc options
              :accept deployments-opt-in)))

(defn create-deployment
  "Creates a deployment for a ref.
  Options are: force, payload, auto-merge, description"
  [user repo ref options]
  (api-call :post "repos/%s/%s/deployments" [user repo]
            (assoc options
              :ref ref
              :accept deployments-opt-in)))

(defn deployment-statuses
  "Returns deployment statuses for a deployment"
  [user repo deployment options]
  (api-call :get "repos/%s/%s/deployments/%s/statuses" [user repo deployment]
            (assoc options
              :accept deployments-opt-in)))

(defn create-deployment-status
  "Create a deployment status.
  Options are: state (required), target-url, description"
  [user repo deployment options]
  (api-call :post "repos/%s/%s/deployments/%s/statuses" [user repo deployment]
            (assoc options
              :accept deployments-opt-in)))

;; # Releases api

(defn releases
  "List releases for a repository."
  [user repo]
  (api-call :get "repos/%s/%s/releases" [user repo]))

(defn specific-release
  "Gets a specific release."
  [user repo id & [options]]
  (api-call :get "repos/%s/%s/releases/%s" [user repo id] options))

(defn specific-release-by-tag
  "Gets a specific release by tag."
  [user repo tag & [options]]
  (api-call :get "repos/%s/%s/releases/tags/%s" [user repo tag] options))

(defn create-release
  "Creates a release.
   Options are: tag-name (required), target-commitish, name, body, draft, prerelease"
  [user repo options]
  (api-call :post "repos/%s/%s/releases" [user repo] options))

(defn delete-release
  "Deletes a release."
  [user repo id & [options]]
  (api-call :delete "repos/%s/%s/releases/%s" [user repo id] options))

;; ## Statistics API

(defn contributor-statistics
  "List additions, deletions, and commit counts per contributor"
  [user repo & [options]]
  (api-call :get "repos/%s/%s/stats/contributors" [user repo] options))

(defn commit-activity
  "List weekly commit activiy for the past year"
  [user repo & [options]]
  (api-call :get "repos/%s/%s/stats/commit_activity" [user repo] options))

(defn code-frequency
  "List weekly additions and deletions"
  [user repo & [options]]
  (api-call :get "repos/%s/%s/stats/code_frequency" [user repo] options))

(defn participation
  "List weekly commit count grouped by the owner and all other users"
  [user repo & [options]]
  (api-call :get "repos/%s/%s/stats/participation" [user repo] options))

(defn punch-card
  "List commit count per hour in the day"
  [user repo & [options]]
  (api-call :get "repos/%s/%s/stats/punch_card" [user repo] options))
