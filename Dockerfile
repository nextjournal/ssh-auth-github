FROM borkdude/babashka:0.2.2

COPY ssh-auth-github.clj /ssh-auth-github.clj

CMD ["/ssh-auth-github.clj"]
