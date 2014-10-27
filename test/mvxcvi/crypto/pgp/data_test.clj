(ns mvxcvi.crypto.pgp.data-test
  (:require
    [byte-streams :refer [bytes=]]
    [midje.sweet :refer :all]
    [mvxcvi.crypto.pgp :as pgp]
    [mvxcvi.crypto.pgp.tags :as tags]
    [mvxcvi.crypto.pgp.test-keys :as test-keys]))


(facts "data encryption"
  (let [pubkey (pgp/get-public-key test-keys/pubring "923b1c1c4392318a")]
    (doseq [message ["Foo Bar Baz"
                     "Mary had a little lamb, whose fleece was white as snow."
                     "TODO: a string with UTF-8 characters"]
            algorithm [:aes-128 :aes-256 :blowfish :cast5 :des :twofish]
            compress (cons nil (keys tags/compression-algorithms))
            armor [false true]]
      (facts (str "Message \"" message \"
                  (when compress (str " compressed with " compress))
                  " encrypted with " algorithm
                  " encoded in " (if armor "ascii" "binary"))
        (let [data (.getBytes message)
              ciphertext (pgp/encrypt
                           data pubkey
                           :algorithm algorithm
                           :compress compress
                           :armor armor)]
          (fact "ciphertext bytes differ from data"
            ciphertext =not=> (partial bytes= data))
          (let [get-privkey
                #(some-> test-keys/secring
                         (pgp/get-secret-key %)
                         (pgp/unlock-key "test password"))]
            (fact "decrypting the ciphertext returns plaintext"
              (pgp/decrypt ciphertext get-privkey)
              => (partial bytes= data))))))))
