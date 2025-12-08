package com.example.android_proyecto.Services;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Set;
import java.util.HashSet;

public class SessionManager {

    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_OWNED_RODS = "owned_rods";

    private static final String KEY_EMAIL = "email";
    private static final String KEY_OWNED_RODS_PREFIX = "owned_rods_";

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

    public void saveOwnedRods(Set<String> rods) {
        if (rods == null) return;

        String key = getInventoryKey();
        if (key == null) return;   // aún no sabemos quién es el jugador

        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(key, new HashSet<>(rods));
        editor.apply();
    }

    public Set<String> getOwnedRods() {
        String key = getInventoryKey();
        if (key == null) {
            return new HashSet<>();
        }

        Set<String> stored = sp.getStringSet(key, new HashSet<>());
        return new HashSet<>(stored);
    }

    public void setEmail(String email) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return sp.getString(KEY_EMAIL, null);
    }
    private String getInventoryKey() {
        String email = getEmail();
        if (email == null || email.isEmpty()) {
            return null;
        }
        return KEY_OWNED_RODS_PREFIX + email;
    }

    public void clear() {

        SharedPreferences.Editor editor = sp.edit();

        editor.remove(KEY_TOKEN);
        editor.remove(KEY_EMAIL);

        editor.apply();
    }
}
