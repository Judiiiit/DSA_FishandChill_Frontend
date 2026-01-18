package com.example.android_proyecto.Models;

public class LeaderboardEntry {
    private String username;
    private int totalFishes;
    private String avatarUrl;


    public LeaderboardEntry() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getAvatarUrl() { return avatarUrl; }


    public int getTotalFishes() {
        return totalFishes;
    }

    public void setTotalFishes(int totalFishes) {
        this.totalFishes = totalFishes;
    }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

}
