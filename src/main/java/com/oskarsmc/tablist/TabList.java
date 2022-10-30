package com.oskarsmc.tablist;

import com.google.inject.Inject;
import com.oskarsmc.tablist.configuration.TabSettings;
import com.oskarsmc.tablist.module.GlobalTabList;
import com.oskarsmc.tablist.module.TabListHeaderFooter;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

public class TabList {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private @DataDirectory
    Path dataDirectory;

    private TabSettings tabSettings;

    private GlobalTabList globalTabList;
    private TabListHeaderFooter tabListHeaderFooter;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.tabSettings = new TabSettings(dataDirectory.toFile(), logger);

        if (this.tabSettings.isEnabled()) {
            if (this.tabSettings.getToml().getBoolean("global-tablist.enabled")) {
                this.globalTabList = new GlobalTabList(this, this.proxyServer);
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
}
