package com.example.android_proyecto.Activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.MainActivity;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.AchievementsManager;
import com.example.android_proyecto.Services.SessionManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private Button btnGoGame, btnGoShop, btnLogout;
    private ImageButton btnSettings, btnGroups;
    private FrameLayout settingsPanel;
    private Button btnBackFromSettings;
    private Button btnEventUsers;
    private Button btnLeaderboard;
    private Button btnDeleteAccount;

    private ImageButton btnAchievements;

    private TextView tvProfileUsername, tvProfileEmail, tvProfileCoins, tvProfilePassword;
    private TextView tvWelcomeUser;

    private TextView tvEventCountdown;

    private SessionManager session;
    private ApiService api;
    private AchievementsManager achievements;
    private ActivityResultLauncher<Intent> unityLauncher;

    private SoundPool soundPool;
    private int soundClick;
    private int soundDanger;

    private final Handler eventHandler = new Handler(Looper.getMainLooper());
    private Runnable eventRunnable;

    private static final long EVENT_ROTATION_MS = 10 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        session = new SessionManager(this);
        api = RetrofitClient.getApiService();
        achievements = new AchievementsManager(this);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        soundClick = soundPool.load(this, R.raw.ui_click, 1);
        soundDanger = soundPool.load(this, R.raw.ui_danger, 1);

        btnGoGame = findViewById(R.id.btnGoGame);
        btnGoShop = findViewById(R.id.btnGoShop);
        btnLogout = findViewById(R.id.btnLogout);

        btnSettings = findViewById(R.id.btnSettings);
        btnGroups = findViewById(R.id.btnGroups);

        settingsPanel = findViewById(R.id.settingsPanel);
        btnBackFromSettings = findViewById(R.id.btnBackFromSettings);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);

        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileCoins = findViewById(R.id.tvProfileCoins);
        tvProfilePassword = findViewById(R.id.tvProfilePassword);

        btnEventUsers = findViewById(R.id.btnEventUsers);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnAchievements = findViewById(R.id.btnAchievements);

        tvEventCountdown = findViewById(R.id.tvEventCountdown);

        String username = session.getUsername();
        tvWelcomeUser.setText("Welcome, " + username + "!");

        unityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String unityResult = result.getData().getStringExtra("unity_result");
                        Log.d("UnityReturn", "unity_result=" + unityResult);
                        Toast.makeText(this, "Unity result: " + unityResult, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnGoGame.setOnClickListener(v -> {
            playClick();
            achievements.unlock(AchievementsManager.A_FIRST_STEPS);
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.DSA1.DSA_Proyecto",
                        "com.unity3d.player.UnityPlayerGameActivity"
                ));
                intent.putExtra("token", session.getToken());
                unityLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Install the unity app first", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoShop.setOnClickListener(v -> {
            playClick();
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            playClick();
            doLogout();
        });

        btnSettings.setOnClickListener(v -> {
            playClick();
            openSettings();
        });

        btnGroups.setOnClickListener(v -> {
            playClick();
            startActivity(new Intent(this, GroupsActivity.class));
        });

        btnEventUsers.setOnClickListener(v -> {
            playClick();
            startActivity(new Intent(this, ChooseEventSplitActivity.class));
        });

        btnLeaderboard.setOnClickListener(v -> {
            playClick();
            startActivity(new Intent(this, LeaderboardActivity.class));
        });

        btnAchievements.setOnClickListener(v -> {
            playClick();
            startActivity(new Intent(this, AchievementsActivity.class));
        });

        btnBackFromSettings.setOnClickListener(v -> {
            playClick();
            closeSettings();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            playDanger();
            confirmDeleteAccount();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (settingsPanel.getVisibility() == View.VISIBLE) {
                    playClick();
                    closeSettings();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        updateEventCountdownUI();
    }

    private void playClick() {
        if (soundPool != null) {
            soundPool.play(soundClick, 0.6f, 0.6f, 0, 0, 1f);
        }
    }

    private void playDanger() {
        if (soundPool != null) {
            soundPool.play(soundDanger, 0.9f, 0.9f, 1, 0, 1f);
        }
    }

    private void openSettings() {
        findViewById(R.id.frameLayout2).setVisibility(View.GONE);
        settingsPanel.setVisibility(View.VISIBLE);
        btnBackFromSettings.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.GONE);
        loadProfile();
    }

    private void closeSettings() {
        findViewById(R.id.frameLayout2).setVisibility(View.VISIBLE);
        settingsPanel.setVisibility(View.GONE);
        btnBackFromSettings.setVisibility(View.GONE);
        btnSettings.setVisibility(View.VISIBLE);
    }

    private void loadProfile() {
        String token = session.getToken();

        tvProfileUsername.setText("Username: " + session.getUsername());
        tvProfileEmail.setText("Email: (cargando...)");
        tvProfileCoins.setText("Coins: (cargando...)");
        tvProfilePassword.setText("Password: ********");

        if (token == null) return;

        api.getProfile(token).enqueue(new retrofit2.Callback<com.example.android_proyecto.Models.User>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.android_proyecto.Models.User> call,
                                   retrofit2.Response<com.example.android_proyecto.Models.User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var u = response.body();
                    tvProfileEmail.setText("Email: " + u.getEmail());
                    tvProfileCoins.setText("Coins: " + u.getCoins());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.android_proyecto.Models.User> call, Throwable t) {
                tvProfileEmail.setText("Email: -");
                tvProfileCoins.setText("Coins: -");
            }
        });
    }

    private void doLogout() {
        String token = session.getToken();

        if (token == null) {
            session.clear();
            goToMain();
            return;
        }

        api.logout(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                session.clear();
                goToMain();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                session.clear();
                goToMain();
            }
        });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción es permanente. ¿Seguro que quieres eliminar tu cuenta?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Eliminar", (d, w) -> {
                    playDanger();
                    doDeleteAccount();
                })
                .show();
    }

    private void doDeleteAccount() {
        String token = session.getToken();

        if (token == null) {
            session.clear();
            goToMain();
            return;
        }

        btnDeleteAccount.setEnabled(false);

        api.deleteMe(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnDeleteAccount.setEnabled(true);
                session.clear();
                goToMain();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnDeleteAccount.setEnabled(true);
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static class RotatingEvent {
        final String id;
        final String name;

        RotatingEvent(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private RotatingEvent getActiveEvent(long nowMs) {
        long slot = nowMs / EVENT_ROTATION_MS;
        boolean first = (slot % 2 == 0);
        if (first) return new RotatingEvent("1", "Fishing Storm");
        return new RotatingEvent("2", "Meteor Arrival");
    }

    private long getMillisUntilNextRotation(long nowMs) {
        long inSlot = nowMs % EVENT_ROTATION_MS;
        return EVENT_ROTATION_MS - inSlot;
    }

    private String formatMMSS(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void updateEventCountdownUI() {
        long now = System.currentTimeMillis();
        RotatingEvent ev = getActiveEvent(now);
        long remaining = getMillisUntilNextRotation(now);

        if (tvEventCountdown != null) {
            tvEventCountdown.setText("Active: " + ev.name + "\nNext in: " + formatMMSS(remaining));
        }
    }

    private void startEventCountdown() {
        if (eventRunnable != null) return;

        eventRunnable = new Runnable() {
            @Override
            public void run() {
                updateEventCountdownUI();
                eventHandler.postDelayed(this, 1000);
            }
        };
        eventHandler.post(eventRunnable);
    }

    private void stopEventCountdown() {
        if (eventRunnable != null) {
            eventHandler.removeCallbacks(eventRunnable);
            eventRunnable = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateEventCountdownUI();
        startEventCountdown();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopEventCountdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopEventCountdown();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
