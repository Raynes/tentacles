(ns tentacles.users-test
  (:use tentacles.users
        tentacles.test-utils
        midje.sweet))

(fact "users"
 (let [my (me auth)
       loc (:location my)]
   (user "amalloy") => (contains {:login "amalloy"})
   my => (contains {:login (:user test-info)})
   (edit-user (merge auth {:location "foo"})) => (contains {:location "foo"})
   (edit-user (merge auth {:location loc})) => (contains {:location loc})))

(fact "emails"
 (let [adds (emails auth)
       fake-email ["foo@bar.baz"]]
   adds => (has every? string?)
   (add-emails fake-email auth) => (contains fake-email)
   (delete-emails fake-email auth) =not=> (contains fake-email)))

(fact "followers"
  (followers "Raynes") => (has every? map?)
  (my-followers auth) =>  (has every? map?)
  (following "Raynes") => (has every? map?)
  (my-following auth) =>  (has every? map?)
  (following? (:follows test-info) auth) => truthy
  (following? "ni3rfj34infn34" auth) => falsey
  (follow "defunkt" auth) => truthy
  (unfollow "defunkt" auth) => truthy
  (unfollow "3nv4nvoi45n" auth) => falsey)

(fact "keys"
  (let [ks (keys auth)
        key (first ks)]
    ks => (has every? map?)
    (specific-key (:id key) auth) => key
    (let [new-key (create-key "foo" "ssh-rsa fakefakefakefake" auth)]
      new-key => (contains {:title "foo"})
      (edit-key (:id new-key) (merge auth {:title "bar"})) => (contains {:title "bar"})
      (delete-key (:id new-key) auth) => truthy)))