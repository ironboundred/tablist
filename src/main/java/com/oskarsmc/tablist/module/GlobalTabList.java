package com.oskarsmc.tablist.module;

import com.oskarsmc.tablist.TabList;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GlobalTabList {
    private final ProxyServer proxyServer;
    private final TabList plugin;
    private final UserManager userManager;

    public GlobalTabList(TabList plugin, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        proxyServer.getScheduler().buildTask(plugin, new Runnable() {
            @Override
            public void run() {
                update();
            }
        }).repeat(2000, TimeUnit.MILLISECONDS).schedule();

        userManager = plugin.luckperms.getUserManager();
    }

    @Subscribe
    public void connect(ServerPostConnectEvent event) {
        update();
    }

    @Subscribe
    public void disconnect(DisconnectEvent event) {
        update();
    }

    public void update() {
        for (Player player : this.proxyServer.getAllPlayers()) {
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
                }else{
                    player.getTabList().removeEntry(player1.getUniqueId());
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

            for (TabListEntry entry : player.getTabList().getEntries()) {
                UUID uuid = entry.getProfile().getId();
                Optional<Player> playerOptional = proxyServer.getPlayer(uuid);
                if (playerOptional.isPresent()) {
                    // Update ping
                    entry.setLatency((int)player.getPing());
                } else {
                    player.getTabList().removeEntry(uuid);
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
        User user = null;
        if (userManager.isLoaded(player.getUniqueId())){
            user = userManager.getUser(player.getUniqueId());
        }else{
            try {
                user = userManager.loadUser(player.getUniqueId()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (user != null && !user.getPrimaryGroup().isEmpty()){
            String rank = "[" + user.getPrimaryGroup() + "]";
            switch (user.getPrimaryGroup().toLowerCase()){
                case "endergod":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(71, 62, 200)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "admin":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(143, 0, 155)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "headmod":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(42, 155, 0)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "mod":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(155, 0, 16)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "chatmod":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(155, 75, 0)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "helper":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(255, 103, 208)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "diamondpatreon":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(32, 134, 255)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "emeraldpatreon":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(81, 255, 62)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "goldpatreon":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(200, 136, 15)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "ironpatreon":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(188, 188, 188)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
                case "vip":{
                    return Component.text(server)
                            .color(TextColor.color(30, 127, 155))
                            .append(Component.text(rank)
                                    .color(TextColor.color(200, 155, 0)))
                            .append(Component.text(username)
                                    .color(TextColor.color(188, 188, 188)));
                }
            }
        }
        return Component.text(server)
                .color(TextColor.color(30, 127, 155))
                .append(Component.text(username)
                        .color(TextColor.color(188, 188, 188)));
    }
}
