package com.example.android_proyecto.Services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android_proyecto.Models.AchievementItem;
import com.example.android_proyecto.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchievementsManager {

    private static final String PREFS = "achievements_prefs";

    public static final String A_FIRST_STEPS = "A_FIRST_STEPS";
    public static final String A_OPEN_SHOP = "A_OPEN_SHOP";
    public static final String A_OPEN_LEADERBOARD = "A_OPEN_LEADERBOARD";
    public static final String A_OPEN_EVENTS = "A_OPEN_EVENTS";
    public static final String A_OPEN_GROUPS = "A_OPEN_GROUPS";

    private final SharedPreferences sp;

    public AchievementsManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void unlock(String id) {
        if (id == null) return;
        if (!sp.getBoolean(id, false)) {
            sp.edit().putBoolean(id, true).apply();
        }
    }

    public boolean isUnlocked(String id) {
        if (id == null) return false;
        return sp.getBoolean(id, false);
    }

    public List<AchievementItem> getAll() {
        List<AchievementItem> list = new ArrayList<>();

        list.add(new AchievementItem(
                A_FIRST_STEPS,
                "First Steps",
                "Enter the game for the first time",
                R.drawable.achievements,
                isUnlocked(A_FIRST_STEPS)
        ));

        list.add(new AchievementItem(
                A_OPEN_SHOP,
                "Trader",
                "Open the shop for the first time",
                R.drawable.achievements,
                isUnlocked(A_OPEN_SHOP)
        ));

        list.add(new AchievementItem(
                A_OPEN_LEADERBOARD,
                "Competitive",
                "Open the leaderboard",
                R.drawable.achievements,
                isUnlocked(A_OPEN_LEADERBOARD)
        ));

        list.add(new AchievementItem(
                A_OPEN_EVENTS,
                "Event Curious",
                "Open the events screen",
                R.drawable.achievements,
                isUnlocked(A_OPEN_EVENTS)
        ));

        list.add(new AchievementItem(
                A_OPEN_GROUPS,
                "Social",
                "Open the groups screen",
                R.drawable.achievements,
                isUnlocked(A_OPEN_GROUPS)
        ));

        return list;
    }

    public Set<String> getUnlockedIds() {
        Set<String> out = new HashSet<>();
        List<AchievementItem> all = getAll();
        if (all == null) return out;
        for (AchievementItem it : all) {
            if (it != null && it.isUnlocked()) out.add(it.getId());
        }
        return out;
    }
}
