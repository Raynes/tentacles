(ns tentacles.users-test
  (:use tentacles.users
        tentacles.test-utils
        midje.sweet))

(against-background
 [(around :contents (let [my (me auth)
                          loc (:location my)]
                      ?form))]
 (fact "Returns a user info map."
   (user "amalloy") => (contains {:login "amalloy"}))
 
 (fact "Contains the authenticated user's info."
   my => (contains {:login (:user test-info)}))
 
 (fact "Edits a user's info."
   (against-background
    (after :facts (edit-user (merge auth {:location loc}))))
   (edit-user (merge auth {:location "foo"})) => (contains {:location "foo"})))

(against-background
 [(around :contents (let [adds (emails auth)
                          fake-email ["foo@bar.baz"]]
                      ?form))]
 (fact "A sequence of email strings."
   adds => (has every? string?))
 
 (fact "Contains the added email."
   (add-emails fake-email auth) => (contains fake-email))
 
 (fact "Does not contain the deleted email."
   (delete-emails fake-email auth) =not=> (contains fake-email)))


(fact "A sequence of maps."
  (followers "Raynes") => (has every? map?))

(fact "A sequence of maps."
  (my-followers auth) =>  (has every? map?))

(fact "A sequence of maps."
  (following "Raynes") => (has every? map?))

(fact "A sequence of maps."
  (my-following auth) =>  (has every? map?))

(fact "User does follow this user."
  (following? (:follows test-info) auth) => truthy)

(fact "User does not follow this user."
  (following? "ni3rfj34infn34" auth) => falsey)

(fact "Can follow a user."
  (follow "defunkt" auth) => truthy)

(fact "Can unfollow that user."
  (unfollow "defunkt" auth) => truthy)

(fact "Cannot unfollow a non-existent user."
  (unfollow "3nv4nvoi45n" auth) => falsey)

(against-background
 [(around :contents (let [ks (keys auth)
                          key (first ks)]
                      ?form))]
 (fact "A sequence of maps of keys."
   ks => (has every? map?))
 
 (fact "Can get a specific key with an id."
   (specific-key (:id key) auth) => key))

(against-background
 [(around :contents (let [new-key (create-key "foo" "ssh-rsa fakefakefakefake" auth)]
                      ?form))]
 (fact "Can create a new key."
   new-key => (contains {:title "foo"}))
 
 (fact "Can edit tht key."
   (edit-key (:id new-key) (merge auth {:title "bar"})) => (contains {:title "bar"}))
 
 (fact "Can delete that key."
   (delete-key (:id new-key) auth) => truthy))