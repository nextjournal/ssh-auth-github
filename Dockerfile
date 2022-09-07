FROM babashka/babashka:0.8.2

COPY ssh-auth-github.clj /ssh-auth-github.clj

CMD ["/ssh-auth-github.clj"]
