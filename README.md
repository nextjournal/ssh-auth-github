# SSH Auth Github

This is a small [Babashka](https://github.com/borkdude/babashka/) script that retrieves public keys from a specific team under a specific organization on Github.

You need a `config.edn` next to the script with

```clojure
{:token ""
 :organization ""
 :team ""}

```

Keep in mind that the token **must have** the following permissions: `read:org`, `read:user`, `user:email`


# Acknowledgements

This library was inspired by the [Rust analogous](https://github.com/dalen/ssh-auth-github)