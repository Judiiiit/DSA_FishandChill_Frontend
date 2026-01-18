package com.example.android_proyecto.Services;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;
import java.util.HashSet;

public class SessionManager {

    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String USER_NAME = "username";
    private static final String KEY_JOINED_GROUPS = "joined_groups";
    private static final String KEY_AVATAR_RES_PREFIX = "avatar_res_";
    private static final String KEY_CURRENT_GROUP_PREFIX = "current_group_";
    private static final String KEY_AVATAR_URL_PREFIX = "avatar_url_";
    private static final String KEY_TEAM_NAME_PREFIX = "team_name_";
    private static final String KEY_TEAM_AVATAR_PREFIX = "team_avatar_";

    private final SharedPreferences sp;

    public SessionManager(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public void saveUsername(String username) {
        sp.edit().putString(USER_NAME, username).apply();
    }

    public String getUsername() {
        return sp.getString(USER_NAME, null);
    }

    public Set<Integer> getJoinedGroups() {
        String username = getUsername();
        if (username == null) return new HashSet<>();

        Set<String> stored = sp.getStringSet(KEY_JOINED_GROUPS + username, new HashSet<>());
        Set<Integer> result = new HashSet<>();

        for (String s : stored) {
            try {
                result.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {}
        }

        return result;
    }

    public void addJoinedGroup(int groupId) {
        String username = getUsername();
        if (username == null) return;

        Set<String> stored = new HashSet<>(
                sp.getStringSet(KEY_JOINED_GROUPS + username, new HashSet<>())
        );

        stored.add(String.valueOf(groupId));
        sp.edit().putStringSet(KEY_JOINED_GROUPS + username, stored).apply();
    }

    public void saveCurrentGroupId(int groupId) {
        String username = getUsername();
        if (username == null) return;
        sp.edit().putInt(KEY_CURRENT_GROUP_PREFIX + username, groupId).apply();
    }

    public int getCurrentGroupId() {
        String username = getUsername();
        if (username == null) return -1;
        return sp.getInt(KEY_CURRENT_GROUP_PREFIX + username, -1);
    }

    public void clearCurrentGroup() {
        String username = getUsername();
        if (username == null) return;
        sp.edit().remove(KEY_CURRENT_GROUP_PREFIX + username).apply();
    }

    public void saveAvatarResId(int resId) {
        String username = getUsername();
        if (username == null) return;
        sp.edit().putInt(KEY_AVATAR_RES_PREFIX + username, resId).apply();
    }

    public int getAvatarResId(int defaultResId) {
        String username = getUsername();
        if (username == null) return defaultResId;
        return sp.getInt(KEY_AVATAR_RES_PREFIX + username, defaultResId);
    }

    public void saveAvatarUrl(String url) {
        String username = getUsername();
        if (username == null) return;
        sp.edit().putString(KEY_AVATAR_URL_PREFIX + username, url).apply();
    }

    public String getAvatarUrl() {
        String username = getUsername();
        if (username == null) return null;
        return sp.getString(KEY_AVATAR_URL_PREFIX + username, null);
    }

    public void saveTeamName(String teamName) {
        String username = getUsername();
        if (username == null) return;
        sp.edit().putString(KEY_TEAM_NAME_PREFIX + username, teamName).apply();
    }

    public String getTeamName() {
        String username = getUsername();
        if (username == null) return null;
        return sp.getString(KEY_TEAM_NAME_PREFIX + username, null);
    }

    public void clearTeamName() {
        String username = getUsername();
        if (username == null) return;
        sp.edit().remove(KEY_TEAM_NAME_PREFIX + username).apply();
    }

    public void saveTeamAvatarUrl(String teamName, String url) {
        String username = getUsername();
        if (username == null) return;
        if (teamName == null || teamName.trim().isEmpty()) return;
        if (url == null || url.trim().isEmpty()) return;

        String key = KEY_TEAM_AVATAR_PREFIX + username + "_" + teamName.trim().toLowerCase();
        sp.edit().putString(key, url).apply();
    }

    public String getTeamAvatarUrl(String teamName) {
        String username = getUsername();
        if (username == null) return null;
        if (teamName == null || teamName.trim().isEmpty()) return null;

        String key = KEY_TEAM_AVATAR_PREFIX + username + "_" + teamName.trim().toLowerCase();
        return sp.getString(key, null);
    }

    public void clear() {
        String username = getUsername();

        SharedPreferences.Editor editor = sp.edit();
        editor.remove(KEY_TOKEN);

        if (username != null) {
            editor.remove(KEY_AVATAR_RES_PREFIX + username);
            editor.remove(KEY_JOINED_GROUPS + username);
            editor.remove(KEY_CURRENT_GROUP_PREFIX + username);
            editor.remove(KEY_AVATAR_URL_PREFIX + username);
            editor.remove(KEY_TEAM_NAME_PREFIX + username);
        }

        editor.remove(USER_NAME);
        editor.apply();
    }
}
