# tablist <a href="https://discord.gg/KfmcRzv6Gh"><img src="https://img.shields.io/discord/840618521611337759?color=pink&label=Discord&logo=discord&logoColor=pink&style=for-the-badge"></a>

<a href="https://bstats.org/plugin/velocity/send/11443"><img src="https://img.shields.io/bstats/servers/11443?color=green&style=for-the-badge"></a>

Tablist Plugin for velocity
# Configuration
```toml
# tablist
[plugin]
    enabled=true

# Global tablist
[global-tablist]
    enabled=false

[tablist-header-footer]
    enabled=false
    # If you are unsure how to hanle new lines, take a look at https://toml.io/en/ or just use \n
    header="<color:#ff0048>Welcome to </color><color:#ff5900>example network!</color>"
    footer="<light_purple>have fun!</light_purple>"

[list-player-current-server]
    enabled=false

[list-player-luckperm-rank]
    enabled=false

[luckperm-rank-color]
    list=["owner","admin","staff"]
    owner="#CF00DB"
    admin="#473EC8"
    staff="#9B0010"

[developer-info]
    config-version=0.2
```
All options are disabled by default.

# Download
Get the latest release <a href="https://github.com/OskarsMC-Plugins/tablist/releases">here</a>