package com.oskarsmc.tablist.module;

import com.oskarsmc.tablist.TabList;
import com.oskarsmc.tablist.util.TabPlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GlobalTabList {
    private final ProxyServer proxyServer;
    private UserManager userManager = null;
    private final boolean useLuckperm;
    private final boolean displayServer;
    Map<String, String> rankColors;

    public GlobalTabList(TabList plugin, ProxyServer proxyServer, boolean useLuckperm,
                         boolean displayServer, LuckPerms luckPerms, Map<String, String> rankColors) {
        this.proxyServer = proxyServer;
        proxyServer.getScheduler().buildTask(plugin, this::update).repeat(2000, TimeUnit.MILLISECONDS).schedule();
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
        List<TabPlayer> tabPlayerList = new ArrayList<>();
        for (Player player: this.proxyServer.getAllPlayers()){
            tabPlayerList.add(getPlayerEntry(player));
        }
        Collections.sort(tabPlayerList);
        char sortLetter = 'a';
        for (int count = 0; count < tabPlayerList.size(); count ++){
            tabPlayerList.get(count).setSortOrder(sortLetter);
            sortLetter ++;
        }

        for (Player player : this.proxyServer.getAllPlayers()) {
            for (TabPlayer tabPlayer : tabPlayerList) {
                if (!player.getTabList().containsEntry(tabPlayer.getUserID())) {
                    player.getTabList().addEntry(
                            TabListEntry.builder()
                                    .displayName(tabPlayer.formatTabPlayer())
                                    .profile(this.proxyServer.getPlayer(tabPlayer.getUserID()).get().getGameProfile()
                                            .withName(String.valueOf(tabPlayer.getSortOrder())))
                                    .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                                    .tabList(player.getTabList())
                                    .build()
                    );
                }else{
                    player.getTabList().removeEntry(tabPlayer.getUserID());
                    player.getTabList().addEntry(
                            TabListEntry.builder()
                                    .displayName(tabPlayer.formatTabPlayer())
                                    .profile(this.proxyServer.getPlayer(tabPlayer.getUserID()).get().getGameProfile()
                                            .withName(String.valueOf(tabPlayer.getSortOrder())))
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

    public TabPlayer getPlayerEntry(Player player) {
        TabPlayer tabPlayer = new TabPlayer(player.getUsername(), player.getUniqueId());

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
                tabPlayer.setSortRank(user.getPrimaryGroup());
                rank = Component.text("[" + user.getPrimaryGroup() + "]");
                if (rankColors.containsKey(user.getPrimaryGroup())){
                    rank = rank.color(TextColor.fromHexString(rankColors.get(user.getPrimaryGroup())));
                }
            }
        }

        tabPlayer.setUserRank(rank);

        if (player.getCurrentServer().isEmpty()) {
            return tabPlayer;
        }

        if (displayServer){
            tabPlayer.setSortServer(player.getCurrentServer().get().getServerInfo().getName());
            server = Component.text("[" + player.getCurrentServer().get().getServerInfo().getName() + "]")
                    .color(TextColor.color(30, 127, 155));
        }

        tabPlayer.setCurrentServer(server);

        return tabPlayer;
    }
}
