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
import net.kyori.adventure.util.RGBLike;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GlobalTabList {
    private final ProxyServer proxyServer;
    private final TabList plugin;
    private UserManager userManager = null;
    private boolean useLuckperm = false;
    private boolean displayServer = false;
    Map<String, String> rankColors = new HashMap<>();

    public GlobalTabList(TabList plugin, ProxyServer proxyServer, boolean useLuckperm,
                         boolean displayServer, LuckPerms luckPerms, Map<String, String> rankColors) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        proxyServer.getScheduler().buildTask(plugin, new Runnable() {
            @Override
            public void run() {
                update();
            }
        }).repeat(2000, TimeUnit.MILLISECONDS).schedule();
        this.useLuckperm = useLuckperm;
        this.displayServer = displayServer;
        if(useLuckperm) {
            userManager = luckPerms.getUserManager();
        }
        this.rankColors = rankColors;
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

    public Component getPlayerEntry(Player player) {
        String username = player.getUsername();
        if (player.getCurrentServer().isEmpty()) {
            return Component.text(username);
        }
        Component server = Component.text("");
        Component rank = Component.text("");

        if(useLuckperm){
            User user = null;
            if (userManager.isLoaded(player.getUniqueId())) {
                user = userManager.getUser(player.getUniqueId());
            } else {
                try {
                    user = userManager.loadUser(player.getUniqueId()).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            if (user != null && !user.getPrimaryGroup().isEmpty()) {
                rank = Component.text("[" + user.getPrimaryGroup() + "]");
                if (rankColors.containsKey(user.getPrimaryGroup())){
                    rank = rank.color(TextColor.fromHexString(rankColors.get(user.getPrimaryGroup())));
                }
            }
        }

        if (displayServer){
            server = Component.text("[" + player.getCurrentServer().get().getServerInfo().getName() + "]")
                    .color(TextColor.color(30, 127, 155));
        }

        return server
                .append(rank)
                .append(Component.text(username)
                        .color(TextColor.color(188, 188, 188)));
    }
}
