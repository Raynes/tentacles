# An octocat is nothing without her tentacles

Tentacles is a Clojure library for working with the Github v3 API. It supports the entire Github API.

This library is the successor to my old [clj-github](https://github.com/Raynes/clj-github) library. clj-github will no longer be maintained.

## Usage

This is on clojars, of course. Just add `[tentacles "0.5.1"]` to your `:dependencies` in your project.clj file.

### CODE!

The library is very simple. It is a very light wrapper around the Github API. For the most part, it replaces keywords with properly formatted keywords, generates JSON for you, etc. Let's try out a few things.

```clojure
user> (user-repos "amalloy")
; Evaluation aborted.
user> (repos/user-repos "amalloy")
[{:fork false, :pushed_at "2010-12-10T07:37:44Z", :name "ddsolve", :clone_url "https://github.com/amalloy/ddsolve.git", :watchers 1, :updated_at "2011-10-04T02:51:53Z", :html_url "https://github.com/amalloy/ddsolve", :owner {:avatar_url "https://secure.gravatar.com/avatar/1c6d7ce3810fd23f0823bf1df5103cd3?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png", :url "https://api.github.com/users/amalloy", :gravatar_id "1c6d7ce3810fd23f0823bf1df5103cd3", :login "amalloy", :id 368685}, :language "Clojure", :size 1704, :created_at "2010-08-18T16:37:47Z", :private false, :homepage "", :git_url "git://github.com/amalloy/ddsolve.git", :url "https://api.github.com/repos/amalloy/ddsolve", :master_branch nil, :ssh_url "git@github.com:amalloy/ddsolve.git", :open_issues 0, :id 846605, :forks 1, :svn_url "https://svn.github.com/amalloy/ddsolve", :description "Double-dummy solver for contract bridge"} ...]
```

I cut out most of the output there. If you try it yourself, you'll notice that it produces a *ton* of output. How can we limit the output? Easily!

```clojure
user> (repos/user-repos "amalloy" {:per-page 1})
[{:fork false, :pushed_at "2010-12-10T07:37:44Z", :name "ddsolve", :clone_url "https://github.com/amalloy/ddsolve.git", :watchers 1, :updated_at "2011-10-04T02:51:53Z", :html_url "https://github.com/amalloy/ddsolve", :owner {:avatar_url "https://secure.gravatar.com/avatar/1c6d7ce3810fd23f0823bf1df5103cd3?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png", :url "https://api.github.com/users/amalloy", :login "amalloy", :gravatar_id "1c6d7ce3810fd23f0823bf1df5103cd3", :id 368685}, :language "Clojure", :size 1704, :created_at "2010-08-18T16:37:47Z", :private false, :homepage "", :git_url "git://github.com/amalloy/ddsolve.git", :url "https://api.github.com/repos/amalloy/ddsolve", :master_branch nil, :ssh_url "git@github.com:amalloy/ddsolve.git", :open_issues 0, :id 846605, :forks 1, :svn_url "https://svn.github.com/amalloy/ddsolve", :description "Double-dummy solver for contract bridge"}]
```

This time we actually *did* get just one item. We explicitly set the number of items allowed per page to 1. The maximum we can set that to is 100 and the default is 30. We can get specific pages of output the same way by using the `:page` option. Additionally, :all-pages true can be passed, which will return a lazy seq of all items on all pages.

This also introduces an idiom in tentacles: options are a map passed to the last parameter of an API function. The options map also contains authentication data when we need it to:

```clojure
user> (repos/repos {:auth "Raynes:REDACTED" :per-page 1})
[{:fork true, :pushed_at "2011-09-21T05:37:17Z", :name "lein-marginalia", :clone_url "https://github.com/Raynes/lein-marginalia.git", :watchers 1, :updated_at "2011-11-23T03:27:47Z", :html_url "https://github.com/Raynes/lein-marginalia", :owner {:login "Raynes", :avatar_url "https://secure.gravatar.com/avatar/54222b6321f0504e0a312c24e97adfc1?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png", :url "https://api.github.com/users/Raynes", :gravatar_id "54222b6321f0504e0a312c24e97adfc1", :id 54435}, :language "Clojure", :size 180, :created_at "2011-11-23T03:27:47Z", :private false, :homepage "", :git_url "git://github.com/Raynes/lein-marginalia.git", :url "https://api.github.com/repos/Raynes/lein-marginalia", :master_branch nil, :ssh_url "git@github.com:Raynes/lein-marginalia.git", :open_issues 0, :id 2832999, :forks 0, :svn_url "https://svn.github.com/Raynes/lein-marginalia", :description "A Marginalia plugin to Leiningen "}]
```

Default options can be specified via `with-defaults`.

If an API function has no options and authentication would have no uses for that particular call, the options map is not a parameter at all. For API calls that can do different things based on whether or not you are authenticated but authentication is not **required**, then the options map will be an optional argument. For API calls that require authentication to function at all, the options map is a required argument. Any data that is required by an API call is a positional argument to the API functions. The options map only ever contains authentication info and/or optional input.

Authentication is supported by Github user authentication `:auth <username:password>` as demonstrated above, or by oauth or oauth2.  For oauth use `:oauth-token <token>` instead of `:auth` in the options map.  Likewise, for oauth2, include `:client-id <client_id> :client-token <client_token>` in the options map.

You can access useful information returned by the API such as current
rate limits, etags, etc. by checking the response with `core/api-meta`. You can then use this to perform conditional requests against the API. If the data has not changed, the keyword `:tentacles.core/not-modified` will be returned. This does not consume any API call quota.

```clojure
user> (core/api-meta (repos/readme "Raynes" "tentacles" {}))
{:links {nil nil}, :etag "\"f1f3cfabbf0f98e0bbaa7aa424f92e75\"", :last-modified "Mon, 28 Jan 2013 21:13:48 GMT", :call-limit 60, :call-remaining 59}

user> (repos/readme "Raynes" "tentacles" {:etag "\"f1f3cfabbf0f98e0bbaa7aa424f92e75\""})
:tentacles.core/not-modified

user> (repos/readme "Raynes" "tentacles" {:if-modified-since "Mon, 28 Jan 2013 21:13:48 GMT"})
:tentacles.core/not-modified
```

Similarly, you can set an User-Agent to make your requests more friendly and identifiable.

```clojure
user> (repos/readme "Raynes" "tentacles" {:user-agent "MyPhoneApp"})
```

The Github API is massive and great. I can't demonstrate every API call. Everything is generally just as easy as the above examples, and I'm working hard to document things as well as possible, so go explore!

Here are some lovely [Marginalia docs](http://raynes.github.com/tentacles). I also wrote a demonstrational [blog post](http://blog.raynes.me/blog/2011/12/02/waving-our-tentacles/) about Tentacles that I intend to keep updated with future releases.

If you run into something that isn't documented well or you don't understand, look for the API call on the Github API [docs](http://developer.github.com/v3/). If you feel like it, please submit a pull request with improved documentation. Let's make this the most impressive Github API library around!

## Hacking

### Running the tests

In order to run the tests, you need to create a `testinfo.clj` in the root of the checkout with some info required for the tests to run properly. This file is ignored by git, so don't worry about committing auth info. This file should contain a Clojure map like the following:

```clojure
{:user "" ;; Github username
 :pass "" ;; Github password
 :follows ""} ;; Username of a person that this user follows.
```

As more tests are written this information may grow.

## License

Copyright (C) 2011 Anthony Grimes

Distributed under the Eclipse Public License, the same as Clojure.
