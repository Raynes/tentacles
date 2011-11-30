(ns tentacles.data
  "Implements the Git Data API: http://developer.github.com/v3/git/blobs/"
  (:use [tentacles.core :only [api-call]]))

;; ## Blobs

(defn blob
  "Get a blob."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/git/blobs/%s" [user repo sha] options))

(defn create-blob
  "Create a blob."
  [user repo content encoding options]
  (api-call :post "repos/%s/%s/git/blobs" [user repo]
            (assoc options
              :content content
              :encoding encoding)))

;; ## Commits

(defn commit
  "Get a commit."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/git/commits/%s" [user repo sha] options))

(defn create-commit
  "Create a commit.
   Options are:
      parents   -- A sequence of SHAs of the commits that were
                   the parents of this commit. If omitted, the
                   commit will be written as a root commit.
      author    -- A map of the following (string keys):
                   \"name\"  -- Name of the author of the commit.
                   \"email\" -- Email of the author of the commit.
                   \"date\"  -- Timestamp when this commit was authored.
      committer -- A map of the following (string keys):
                   \"name\"  -- Name of the committer of the commit.
                   \"email\" -- Email of the committer of the commit.
                   \"date\"  -- Timestamp when this commit was committed.
   If the committer section is omitted, then it will be filled in with author
   data. If that is omitted, information will be obtained using the
   authenticated user's information and the current date."
  [user repo message tree options]
  (api-call :post "repos/%s/%s/git/commits" [user repo]
            (assoc options
              :message message
              :tree tree
              :parents parents)))

;; ## References

(defn reference
  "Get a reference."
  [user repo ref & [options]]
  (api-call :get "repos/%s/%s/git/refs/%s" [user repo ref] options))

(defn references
  "Get all references."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/git/refs" [user repo] options))

(defn create-reference
  "Create a new reference."
  [user repo ref sha options]
  (api-call :post "repos/%s/%s/git/refs" [user repo]
            (assoc options
              :ref ref
              :sha sha)))

(defn edit-reference
  "Edit a reference.
   Options are:
      force -- true or false (default); whether to force the update or make
               sure this is a fast-forward update."
  [user repo ref sha options]
  (api-call :post "repos/%s/%s/git/refs/%s" [user repo ref]
            (assoc options :sha sha)))


;; ## Tags

(defn tag
  "Get a tag."
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/git/tags/%s" [user repo sha] options))

;; The API documentation is unclear about which parts of this API call
;; are optional.
(defn create-tag
  "Create a tag object. Note that this does not create the reference
   that makes a tag in Git. If you want to create an annotated tag, you
   have to do this call to create the tag object and then create the
   `refs/tags/[tag]` reference. If you want to create a lightweight tag,
   you simply need to create the reference and this call would be
   unnecessary.
   Options are:
      tagger -- A map (string keys) containing the following:
                \"name\"  -- Name of the author of this tag.
                \"email\" -- Email of the author of this tag.
                \"date\"  -- Timestamp when this object was tagged."
  [user repo tag message object type options]
  (api-call :post "repos/%s/%s/git/tags" [user repo]
            (assoc options
              :tag tag
              :message message
              :object object
              :type type)))

;; ## Trees

(defn tree
  "Get a tree.
   Options are:
      recursive -- true or false; get a tree recursively?"
  [user repo sha & [options]]
  (api-call :get "repos/%s/%s/git/trees/%s" [user repo sha] options))

(defn create-tree
  "Create a tree. 'tree' is a map of the following (string keys):
   path    -- The file referenced in the tree.
   mode    -- The file mode; one of 100644 for file, 100755 for executable,
              040000 for subdirectory, 160000 for submodule, or 120000 for
              a blob that specifies the path of a symlink.
   type    -- blog, tree, or commit.
   sha     -- SHA of the object in the tree.
   content -- Content that you want this file to have.
   Options are:
      base-tree -- SHA of the tree you want to update (if applicable)."
  [user repo tree options]
  (api-call :post "repos/%s/%s/git/trees" [user repo]
            (assoc options :tree tree)))

