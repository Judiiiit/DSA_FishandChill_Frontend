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
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.AvatarAdapter;
import com.example.android_proyecto.MainActivity;
import com.example.android_proyecto.Models.AvatarItem;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.AchievementsManager;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bumptech.glide.Glide;
import android.graphics.drawable.PictureDrawable;
import com.example.android_proyecto.glide.SvgSoftwareLayerSetter;

import android.widget.ImageView;


public class MenuActivity extends AppCompatActivity {

    private Button btnGoGame, btnGoShop, btnLogout;
    private ImageButton btnSettings, btnGroups, btnAchievements;
    private Button btnEventUsers, btnLeaderboard, btnDeleteAccount, btnBackFromSettings, btnChooseAvatar;
    private FrameLayout settingsPanel;

    private TextView tvProfileUsername, tvProfileEmail, tvProfileCoins, tvProfilePassword;
    private TextView tvWelcomeUser, tvEventCountdown;

    private SessionManager session;
    private ApiService api;
    private AchievementsManager achievements;

    private ActivityResultLauncher<Intent> unityLauncher;

    private SoundPool soundPool;
    private int soundClick;
    private int soundDanger;

    private final Handler eventHandler = new Handler(Looper.getMainLooper());
    private Runnable eventRunnable;

    private ImageView ivSettingsAvatarBig;
    private static final long EVENT_ROTATION_MS = 10 * 60 * 1000L;

    private boolean isSettingsOpen = false;

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
        btnAchievements = findViewById(R.id.btnAchievements);

        btnEventUsers = findViewById(R.id.btnEventUsers);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);

        settingsPanel = findViewById(R.id.settingsPanel);
        btnBackFromSettings = findViewById(R.id.btnBackFromSettings);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnChooseAvatar = findViewById(R.id.btnChooseAvatar);

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileCoins = findViewById(R.id.tvProfileCoins);
        tvProfilePassword = findViewById(R.id.tvProfilePassword);
        tvEventCountdown = findViewById(R.id.tvEventCountdown);

        ivSettingsAvatarBig = findViewById(R.id.ivSettingsAvatarBig);
        ivSettingsAvatarBig.setImageResource(R.drawable.avatar_1);

        setSettingsOpen(false);   // fuerza el estado correcto al inicio

        btnSettings.setImageResource(R.drawable.avatar_1);

        String cachedAvatarUrl = session.getAvatarUrl();
        if (cachedAvatarUrl != null && !cachedAvatarUrl.isEmpty()) {
            loadAvatarIntoSettingsButton(cachedAvatarUrl);
        }

        loadProfile();


        tvWelcomeUser.setText("Welcome, " + session.getUsername() + "!");

        btnChooseAvatar.setOnClickListener(v -> {
            playClick();
            changeAvatarOnServer();
        });

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
            achievements.unlock(AchievementsManager.A_OPEN_SHOP);
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnGroups.setOnClickListener(v -> {
            playClick();
            achievements.unlock(AchievementsManager.A_OPEN_GROUPS);
            startActivity(new Intent(this, GroupsActivity.class));
        });

        btnEventUsers.setOnClickListener(v -> {
            playClick();
            achievements.unlock(AchievementsManager.A_OPEN_EVENTS);
            startActivity(new Intent(this, ChooseEventSplitActivity.class));
        });

        btnLeaderboard.setOnClickListener(v -> {
            playClick();
            achievements.unlock(AchievementsManager.A_OPEN_LEADERBOARD);
            startActivity(new Intent(this, LeaderboardActivity.class));
        });

        btnAchievements.setOnClickListener(v -> {
            playClick();
            startActivity(new Intent(this, AchievementsActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            playClick();
            openSettings();
        });

        btnBackFromSettings.setOnClickListener(v -> {
            playClick();
            closeSettings();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            playDanger();
            confirmDeleteAccount();
        });

        btnLogout.setOnClickListener(v -> {
            playClick();
            doLogout();
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
        startEventCountdown();
    }

    private void openSettings() {
        playClick();
        setSettingsOpen(true);
        loadProfile();
    }

    private void closeSettings() {
        playClick();
        setSettingsOpen(false);
    }

    private void setSettingsOpen(boolean open) {
        isSettingsOpen = open;

        settingsPanel.setVisibility(open ? View.VISIBLE : View.GONE);
        findViewById(R.id.frameLayout2).setVisibility(open ? View.GONE : View.VISIBLE);

        btnSettings.setVisibility(open ? View.GONE : View.VISIBLE);
        btnBackFromSettings.setVisibility(open ? View.VISIBLE : View.GONE);

        if (ivSettingsAvatarBig != null) ivSettingsAvatarBig.setVisibility(open ? View.VISIBLE : View.GONE);

        btnLeaderboard.setVisibility(open ? View.GONE : View.VISIBLE);
        btnAchievements.setVisibility(open ? View.GONE : View.VISIBLE);
        btnEventUsers.setVisibility(open ? View.GONE : View.VISIBLE);
        btnGroups.setVisibility(open ? View.GONE : View.VISIBLE);
        if (tvEventCountdown != null) tvEventCountdown.setVisibility(open ? View.GONE : View.VISIBLE);

        if (!open) {
            String url = session != null ? session.getAvatarUrl() : null;
            if (url != null && !url.trim().isEmpty()) {
                refreshAvatars(url);
            } else {
                btnSettings.setVisibility(View.VISIBLE);
                btnSettings.setImageResource(R.drawable.avatar_1);
            }
        }
    }


    private void showAvatarPickerDialog() {
        int current = session.getAvatarResId(R.drawable.avatar_1);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_avatar_picker, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvAvatars);
        Button btnClose = dialogView.findViewById(R.id.btnCloseAvatarPicker);
        TextView tvHint = dialogView.findViewById(R.id.tvAvatarHint);

        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setHasFixedSize(true);

        java.util.List<AvatarItem> items = new java.util.ArrayList<>();
        items.add(new AvatarItem(R.drawable.avatar_1, null, ""));
        items.add(new AvatarItem(R.drawable.avatar_2, AchievementsManager.A_FIRST_STEPS, "Unlock: First Steps"));
        items.add(new AvatarItem(R.drawable.avatar_3, AchievementsManager.A_OPEN_SHOP, "Unlock: Trader"));
        items.add(new AvatarItem(R.drawable.avatar_4, AchievementsManager.A_OPEN_LEADERBOARD, "Unlock: Competitive"));
        items.add(new AvatarItem(R.drawable.avatar_5, AchievementsManager.A_OPEN_EVENTS, "Unlock: Event Curious"));
        items.add(new AvatarItem(R.drawable.avatar_6, AchievementsManager.A_OPEN_GROUPS, "Unlock: Social"));

        java.util.Set<String> unlocked = achievements.getUnlockedIds();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        AvatarAdapter adapter = new AvatarAdapter(items, unlocked, current, (item, isUnlocked) -> {
            if (!isUnlocked) {
                String msg = item.getLockedText();
                if (msg == null || msg.isEmpty()) msg = "Locked";
                tvHint.setText(msg);
                tvHint.setVisibility(View.VISIBLE);
                return;
            }

            tvHint.setVisibility(View.GONE);
            session.saveAvatarResId(item.getResId());
            btnSettings.setImageResource(item.getResId());
            dialog.dismiss();
        });

        rv.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void loadProfile() {
        String token = session.getToken();

        tvProfileUsername.setText("Username: " + session.getUsername());
        tvProfileEmail.setText("Email: (cargando...)");
        tvProfileCoins.setText("Coins: (cargando...)");
        tvProfilePassword.setText("Password: ********");

        if (token == null) return;

        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvProfileEmail.setText("Email: -");
                    tvProfileCoins.setText("Coins: -");
                    return;
                }

                User u = response.body();
                tvProfileEmail.setText("Email: " + u.getEmail());
                tvProfileCoins.setText("Coins: " + u.getCoins());

                String avatarUrl = u.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    session.saveAvatarUrl(avatarUrl);
                    refreshAvatars(avatarUrl);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
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

    private void updateEventCountdownUI() {
        long now = System.currentTimeMillis();
        long remaining = EVENT_ROTATION_MS - (now % EVENT_ROTATION_MS);
        long min = remaining / 60000;
        long sec = (remaining / 1000) % 60;
        if (tvEventCountdown != null) {
            tvEventCountdown.setText("Active: Fishing Storm\nNext in: " + String.format("%02d:%02d", min, sec));
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

    private void playClick() {
        if (soundPool != null) soundPool.play(soundClick, 0.6f, 0.6f, 0, 0, 1f);
    }

    private void playDanger() {
        if (soundPool != null) soundPool.play(soundDanger, 0.9f, 0.9f, 1, 0, 1f);
    }

    private void loadAvatarIntoSettingsButton(String url) {
        if (url == null || url.trim().isEmpty()) {
            btnSettings.setImageResource(R.drawable.avatar_1);
            return;
        }

        String u = url.trim().toLowerCase();

        if (u.contains("/svg") || u.contains("svg?") || u.endsWith(".svg")) {
            Glide.with(this)
                    .as(PictureDrawable.class)
                    .load(url)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .listener(new SvgSoftwareLayerSetter())
                    .into(btnSettings);
            return;
        }

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.avatar_1)
                .error(R.drawable.avatar_1)
                .circleCrop()
                .into(btnSettings);
    }


    private void changeAvatarOnServer() {
        String token = session.getToken();
        if (token == null) return;

        api.changeAvatar(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    String raw = response.body().string();
                    if (raw == null) return;

                    String url = raw.trim();

                    // Por si viene como "https://...."
                    if (url.startsWith("\"") && url.endsWith("\"") && url.length() >= 2) {
                        url = url.substring(1, url.length() - 1);
                    }

                    session.saveAvatarUrl(url);

                    // Limpia imágenes anteriores (Glide)
                    if (!isSettingsOpen) {
                        Glide.with(MenuActivity.this).clear(btnSettings);
                    }
                    if (ivSettingsAvatarBig != null) {
                        Glide.with(MenuActivity.this).clear(ivSettingsAvatarBig);
                    }

                    // Placeholder mientras carga (si quieres)
                    if (!isSettingsOpen) {
                        btnSettings.setImageResource(R.drawable.avatar_1);
                    }
                    if (ivSettingsAvatarBig != null) {
                        ivSettingsAvatarBig.setImageResource(R.drawable.avatar_1);
                    }

                    // Carga el nuevo avatar en los sitios permitidos
                    refreshAvatars(url);

                } catch (Exception ignored) {}
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void refreshAvatars(String url) {
        if (!isSettingsOpen) {
            btnSettings.setVisibility(View.VISIBLE);
            loadAvatarInto(btnSettings, url);
        }

        if (isSettingsOpen && ivSettingsAvatarBig != null) {
            ivSettingsAvatarBig.setVisibility(View.VISIBLE);
            loadAvatarInto(ivSettingsAvatarBig, url);
        }
    }


    private void loadAvatarInto(ImageView target, String url) {
        if (target == null) return;

        if (url == null || url.trim().isEmpty()) {
            // si no hay url, puedes decidir si mostrar default o dejar invisible
            target.setImageDrawable(null);
            target.setVisibility(View.INVISIBLE);
            return;
        }

        String u = url.trim().toLowerCase();

        if (u.contains("/svg") || u.contains("svg?") || u.endsWith(".svg")) {
            Glide.with(MenuActivity.this)
                    .as(PictureDrawable.class)
                    .load(url)
                    .error(R.drawable.avatar_1)
                    .listener(new SvgSoftwareLayerSetter())
                    .listener(new com.bumptech.glide.request.RequestListener<PictureDrawable>() {
                        @Override
                        public boolean onLoadFailed(
                                com.bumptech.glide.load.engine.GlideException e,
                                Object model,
                                com.bumptech.glide.request.target.Target<PictureDrawable> targetGlide,
                                boolean isFirstResource
                        ) {
                            showAvatar(target);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                PictureDrawable resource,
                                Object model,
                                com.bumptech.glide.request.target.Target<PictureDrawable> targetGlide,
                                com.bumptech.glide.load.DataSource dataSource,
                                boolean isFirstResource
                        ) {
                            showAvatar(target);
                            return false;
                        }
                    })
                    .into(target);
            return;
        }

        Glide.with(MenuActivity.this)
                .load(url)
                .error(R.drawable.avatar_1) // opcional
                .circleCrop()
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            com.bumptech.glide.load.engine.GlideException e,
                            Object model,
                            com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> targetGlide,
                            boolean isFirstResource
                    ) {
                        showAvatar(target);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            android.graphics.drawable.Drawable resource,
                            Object model,
                            com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> targetGlide,
                            com.bumptech.glide.load.DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        showAvatar(target);
                        return false;
                    }
                })
                .into(target);
    }


    private void hideAvatar(ImageView v) {
        if (v == null) return;
        Glide.with(MenuActivity.this).clear(v);
        v.setImageDrawable(null);
        v.setVisibility(View.INVISIBLE);
    }

    private void showAvatar(ImageView v) {
        if (v == null) return;
        v.setVisibility(View.VISIBLE);
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
