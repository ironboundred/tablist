package com.oskarsmc.tablist;

import com.google.inject.Inject;
import com.oskarsmc.tablist.configuration.TabSettings;
import com.oskarsmc.tablist.module.GlobalTabList;
import com.oskarsmc.tablist.module.TabListHeaderFooter;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(
        id = "tablist",
        name = "tablist",
        version = "0.0.1",
        description = "Tablist Plugin for velocity",
        authors = {"OskarsMC","OskarZyg","Ironboundred"},
        dependencies = {@Dependency(id = "luckperms")}
)
public class TabList {
    private static TabList tabList;
    @Inject
    private Logger logger;

    @Inject
    public ProxyServer proxyServer;

    @Inject
    private @DataDirectory
    Path dataDirectory;

    private TabSettings tabSettings;

    private GlobalTabList globalTabList;
    private TabListHeaderFooter tabListHeaderFooter;

    public LuckPerms luckperms;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        tabList = this;

        this.tabSettings = new TabSettings(dataDirectory.toFile(), logger);

        if (this.tabSettings.getToml().getBoolean("list-player-luckperm-rank.enabled")) {
            try {
                luckperms = LuckPermsProvider.get();
            } catch (IllegalArgumentException e) {
                logger.info("Unable  to load LuckPerms!");
            }
        }

        if (this.tabSettings.isEnabled()) {
            Map<String, String> rankColors = new HashMap<>();
            List<String> rankList = this.tabSettings.getToml().getList("luckperm-rank-color.list");
            for (String s: rankList){
                rankColors.put(s, this.tabSettings.getToml().getString("luckperm-rank-color." + s));
            }
            if (this.tabSettings.getToml().getBoolean("global-tablist.enabled")) {
                this.globalTabList = new GlobalTabList(
                        this.tabSettings.getToml().getBoolean("list-player-luckperm-rank.enabled"),
                        this.tabSettings.getToml().getBoolean("list-player-current-server.enabled"),
                        rankColors);
                this.proxyServer.getEventManager().register(this, this.globalTabList);
                logger.info("Loaded Global Tablist");
            }

            if (this.tabSettings.getToml().getBoolean("tablist-header-footer.enabled")) {
                this.tabListHeaderFooter = new TabListHeaderFooter(this, this.proxyServer, this.tabSettings);
                this.proxyServer.getEventManager().register(this, this.tabListHeaderFooter);
                logger.info("Loaded Header & Footer");
            }
        }
    }

    public static TabList getInstance(){
        return tabList;
    }
    public int getRankWeight(String rank){
        int weight = 0;

        if (this.tabSettings.getToml().getBoolean("list-player-luckperm-rank.enabled")){
            if (this.luckperms.getGroupManager().getGroup(rank) == null){
                return weight;
            }

            if (this.luckperms.getGroupManager().getGroup(rank).getWeight().isPresent()) {
                weight = this.luckperms.getGroupManager().getGroup(rank).getWeight().getAsInt();
            }
        }

        return weight;
    }
}
