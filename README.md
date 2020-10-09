# SSH Auth Github

This is a small [Babashka](https://github.com/borkdude/babashka/) script that retrieves public keys from a specific team under a specific organization on Github.
This can be useful to essentially use GitHub teams as access control, making sure that only the people in a certain team
have SSH access to machines. 

This script can be used as `AuthorizedKeysCommand` or piped into `authorized_keys`.


## Usage

You need a `config.edn` next to the script with

```clojure
{:token ""
 :organization ""
 :team ""}

```

Keep in mind that the token **must have** the following permissions: `read:org`, `read:user`, `user:email`


# Acknowledgements

This library was inspired by the [Rust analogous](https://github.com/dalen/ssh-auth-github)