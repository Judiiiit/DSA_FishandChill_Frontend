package com.example.android_proyecto.Models;

public class AchievementItem {

    private final String id;
    private final String title;
    private final String description;
    private final int iconRes;
    private final boolean unlocked;

    public AchievementItem(String id, String title, String description, int iconRes, boolean unlocked) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.unlocked = unlocked;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public boolean isUnlocked() { return unlocked; }
}
