{
  outputs = inputs: {
    nixosModules.ssh-auth-github = {
      lib,
      pkgs,
      config,
      ...
    }: let
      cfg = config.services.ssh-auth-github;
      cmd = pkgs.writeShellApplication {
        name = "ssh-auth-github";
        runtimeInputs = with pkgs; [ babashka curl];
        text = ''
          bb ${./ssh-auth-github.clj} --token "$(cat ${cfg.tokenFile})" --organization ${cfg.organization} --team ${cfg.team} $@
        '';
      };
    in
      with lib; {
        options = {
          services.ssh-auth-github = {
            enable = mkEnableOption "ssh-auth-github";
            organization = mkOption {
              description = mdDoc "GitHub organization";
              type = types.str;
            };
            team = mkOption {
              description = mdDoc "GitHub team";
              type = types.str;
            };
            tokenFile = mkOption {
              description = mdDoc "GitHub access token with `read:org`, `read:user`, `user:email` permissions";
              type = types.path;
            };
            authorizedKeysCommandUser = mkOption {
              description = mdDoc "User to run the authorized_keys_command under. Needs read access to `tokenFile`.";
              type = types.str;
              default = "nobody";
            };
          };
        };
        config = mkIf cfg.enable {
          # Ugly: sshd refuses to start if a store path is given because /nix/store is group-writable.
          # So indirect by a symlink.
          environment.etc."ssh/github_authorized_keys_command" = {
            mode = "0755";
            text = ''
              #!/bin/sh
              exec ${cmd} "$@"
            '';
          };
          services.openssh.authorizedKeysCommand = "/etc/ssh/github_authorized_keys_command";
          services.openssh.authorizedKeysCommandUser = cfg.authorizedKeysCommandUser;
        };
      };
  };
}
