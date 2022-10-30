package com.oskarsmc.tablist.module;

import com.oskarsmc.tablist.TabList;
import com.oskarsmc.tablist.configuration.TabSettings;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.concurrent.TimeUnit;

public class TabListHeaderFooter {
    private Component header;
    private Component footer;
    private ProxyServer proxyServer;

    public TabListHeaderFooter(TabList plugin, ProxyServer proxyServer, TabSettings tabSettings) {
        var mm = MiniMessage.miniMessage();
        this.header = mm.deserialize(tabSettings.getToml().getString("tablist-header-footer.header"));
        this.footer = mm.deserialize(tabSettings.getToml().getString("tablist-header-footer.footer"));

        this.proxyServer = proxyServer;

        this.proxyServer.getScheduler().buildTask(plugin, TabListHeaderFooter.this::update).repeat(50, TimeUnit.MILLISECONDS).schedule();
    }

    public void update() {
        for (Player player : this.proxyServer.getAllPlayers()) {
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }
}
