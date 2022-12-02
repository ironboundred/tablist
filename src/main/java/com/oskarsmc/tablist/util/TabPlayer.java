package com.oskarsmc.tablist.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TabPlayer implements Comparable<TabPlayer> {
    private final String username;
    private final UUID userID;
    private Component currentServer;
    private Component userRank;
    private String sortRank = "";
    private String sortServer = "";
    private char sortOrder;

    public TabPlayer(String username, UUID userID){
        this.username = username;
        this.userID = userID;
    }

    @Override
    public int compareTo(@NotNull TabPlayer player){
        if (userRank == null || player.getUserRank() == null){
            return 0;
        }
        if (sortServer.toLowerCase().compareTo(player.getSortServer().toLowerCase()) == 0){
            if (sortRank.toLowerCase().compareTo(player.getSortRank().toLowerCase()) == 0){
                return username.toLowerCase().compareTo(player.getUsername().toLowerCase());
            }
            return sortRank.toLowerCase().compareTo(player.getSortRank().toLowerCase());
        }
        return sortServer.toLowerCase().compareTo(player.getSortServer().toLowerCase());
    }

    public Component formatTabPlayer(){
        return getCurrentServer()
                .append(getUserRank())
                .append(Component.text(getUsername())
                        .color(TextColor.color(188, 188, 188)));
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserID() {
        return userID;
    }

    public Component getCurrentServer() {
        return currentServer;
    }

    public Component getUserRank() {
        return userRank;
    }

    public char getSortOrder() {
        return sortOrder;
    }

    public String getSortRank() {
        return sortRank;
    }

    public String getSortServer() {
        return sortServer;
    }

    public void setCurrentServer(Component currentServer) {
        this.currentServer = currentServer;
    }

    public void setUserRank(Component userRank) {
        this.userRank = userRank;
    }

    public void setSortOrder(char sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSortRank(String sortRank) {
        this.sortRank = sortRank;
    }

    public void setSortServer(String sortServer) {
        this.sortServer = sortServer;
    }
}
