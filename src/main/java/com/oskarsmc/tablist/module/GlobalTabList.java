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

import javax.swing.text.html.Option;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GlobalTabList {
    private final ProxyServer proxyServer;
    private UserManager userManager = null;
    private final boolean useLuckperm;
    private final boolean displayServer;
    Map<String, String> rankColors;

    public GlobalTabList(boolean useLuckperm, boolean displayServer, Map<String, String> rankColors) {
        this.proxyServer = TabList.getInstance().proxyServer;
        proxyServer.getScheduler().buildTask(TabList.getInstance(), this::update).repeat(2000, TimeUnit.MILLISECONDS).schedule();
        this.useLuckperm = useLuckperm;
        this.displayServer = displayServer;
        if(useLuckperm) {
            userManager = TabList.getInstance().luckperms.getUserManager();
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

        char sortLetter = 'A';
        for (TabPlayer value : tabPlayerList) {
            value.setSortOrder(sortLetter);
            sortLetter++;
        }

        for (Player player : this.proxyServer.getAllPlayers()) {
            for (TabPlayer tabPlayer : tabPlayerList) {
                if (player.getTabList().containsEntry(tabPlayer.getUserID())) {
                    player.getTabList().removeEntry(tabPlayer.getUserID());
                }

                player.getTabList().addEntry(
                        TabListEntry.builder()
                                .displayName(tabPlayer.formatTabPlayer())
                                .profile(this.proxyServer.getPlayer(tabPlayer.getUserID()).get().getGameProfile()
                                        .withName(String.valueOf(tabPlayer.getSortOrder()) + " " + shorten(tabPlayer.getUsername())))
                                .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                                .tabList(player.getTabList())
                                .build()
                );
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
                }else {
                    rank = rank.color(TextColor.color(188, 188, 188));
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

    private String shorten(String string){
        return Optional.ofNullable(string)
                .filter(str -> str.length() >= 15)
                .map(str -> str.substring(0, str.length() - 2))
                .orElse(string);
    }
}
