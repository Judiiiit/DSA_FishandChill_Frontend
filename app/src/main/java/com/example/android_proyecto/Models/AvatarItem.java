package com.example.android_proyecto.Models;

public class AvatarItem {
    private final int resId;
    private final String requiredAchievementId;
    private final String lockedText;

    public AvatarItem(int resId, String requiredAchievementId, String lockedText) {
        this.resId = resId;
        this.requiredAchievementId = requiredAchievementId;
        this.lockedText = lockedText;
    }

    public int getResId() {
        return resId;
    }

    public String getRequiredAchievementId() {
        return requiredAchievementId;
    }

    public String getLockedText() {
        return lockedText;
    }
}
