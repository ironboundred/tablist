package com.oskarsmc.tablist.module;

import com.oskarsmc.tablist.TabList;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GlobalTabList {
    private final ProxyServer proxyServer;
    private final TabList plugin;

    public GlobalTabList(TabList plugin, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        proxyServer.getScheduler().buildTask(plugin, new Runnable() {
            @Override
            public void run() {
                update();
            }
        }).repeat(500, TimeUnit.MILLISECONDS);
    }

    @Subscribe
    public void connect(ServerConnectedEvent event) {
        update();
    }

    @Subscribe
    public void disconnect(DisconnectEvent event) {
        update();
    }

    public void update() {
        for (Player player : this.proxyServer.getAllPlayers()) {
            for (TabListEntry entry : player.getTabList().getEntries()) {
                UUID uuid = entry.getProfile().getId();
                Optional<Player> playerOptional = proxyServer.getPlayer(uuid);
                if (playerOptional.isPresent()) {
                    // Update ping
                    entry.setLatency((int) (player.getPing() * 1000));
                } else {
                    player.getTabList().removeEntry(uuid);
                }
            }

            for (Player player1 : this.proxyServer.getAllPlayers()) {
                if (!player.getTabList().containsEntry(player1.getUniqueId())) {
                    player.getTabList().addEntry(
                            TabListEntry.builder()
                                    .displayName(getPlayerEntry(player1))
                                    .profile(player1.getGameProfile())
                                    .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                                    .tabList(player.getTabList())
                                    .build()
                    );
                }
            }
        }
    }

    public Component getPlayerEntry(Player player){
        String username = player.getUsername();
        if (player.getCurrentServer().isEmpty()){
            return Component.text(username);
        }
        String server = "[" + player.getCurrentServer().get().getServerInfo().getName() + "]";
        if (plugin.luckperms.getUserManager().getUser(player.getUniqueId()) != null &&
                !plugin.luckperms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup().toLowerCase().equals("cyberdog")){
            String rank = "[" + plugin.luckperms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup() + "]";
            return Component.text(server)
                    .color(TextColor.color(30, 127, 155))
                    .append(Component.text(rank)
                            .color(TextColor.color(155, 130, 41)))
                    .append(Component.text(username)
                            .color(TextColor.color(188, 188, 188)));
        }

        return Component.text(server)
                .color(TextColor.color(30, 127, 155))
                .append(Component.text(username)
                        .color(TextColor.color(188, 188, 188)));
    }
}
