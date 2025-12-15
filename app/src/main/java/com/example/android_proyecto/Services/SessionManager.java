package com.example.android_proyecto.Services;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Set;
import java.util.HashSet;

public class SessionManager {

    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_TOKEN = "token";

    private static final String USER_NAME = "username";


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




    public void clear() {

        SharedPreferences.Editor editor = sp.edit();

        editor.remove(KEY_TOKEN);
        editor.remove(USER_NAME);


        editor.apply();
    }
}
