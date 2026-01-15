package com.example.android_proyecto.Activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
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

    private TextView tvProfileUsername, tvProfileEmail, tvProfileCoins, tvProfilePassword;
    private TextView tvWelcomeUser;
    private SessionManager session;
    private ApiService api;
    private ActivityResultLauncher<Intent> unityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        session = new SessionManager(this);
        api = RetrofitClient.getApiService();

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
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());

        String username = session.getUsername();
        tvWelcomeUser.setText("Welcome, " + username + "!");

        unityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String unityResult = result.getData().getStringExtra("unity_result");
                        Log.d("UnityReturn", "unity_result=" + unityResult);
                        Toast.makeText(this, "Unity result: " + unityResult, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("UnityReturn", "Unity cancelled or without data");
                    }
                }
        );

        btnGoGame.setOnClickListener(v -> {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.DSA1.DSA_Proyecto",
                        "com.unity3d.player.UnityPlayerGameActivity"
                ));
                intent.putExtra("token", session.getToken());
                unityLauncher.launch(intent);

            } catch (Exception e) {
                Toast.makeText(MenuActivity.this, "Install the unity app first", Toast.LENGTH_SHORT).show();
                Log.e("UnityLaunchError", "Error launching Unity", e);
            }
        });

        btnGoShop.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> doLogout());

        btnSettings.setOnClickListener(v -> openSettings());

        btnGroups.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GroupsActivity.class);
            startActivity(intent);
        });

        btnEventUsers.setOnClickListener(v -> {
            Intent i = new Intent(MenuActivity.this, ChooseEventSplitActivity.class);
            startActivity(i);
        });

        btnLeaderboard.setOnClickListener(v -> {
            Intent i = new Intent(MenuActivity.this, LeaderboardActivity.class);
            startActivity(i);
        });

        btnBackFromSettings.setOnClickListener(v -> closeSettings());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (settingsPanel != null && settingsPanel.getVisibility() == View.VISIBLE) {
                    closeSettings();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
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

        String localUsername = session.getUsername();
        tvProfileUsername.setText("Username: " + (localUsername != null ? localUsername : "-"));
        tvProfileEmail.setText("Email: (cargando...)");
        tvProfileCoins.setText("Coins: (cargando...)");
        tvProfilePassword.setText("Password: ********");

        if (token == null) {
            tvProfileEmail.setText("Email: -");
            tvProfileCoins.setText("Coins: -");
            return;
        }

        api.getProfile(token).enqueue(new retrofit2.Callback<com.example.android_proyecto.Models.User>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.android_proyecto.Models.User> call,
                                   retrofit2.Response<com.example.android_proyecto.Models.User> response) {

                if (response.isSuccessful() && response.body() != null) {
                    com.example.android_proyecto.Models.User u = response.body();

                    tvProfileUsername.setText("Username: " + (u.getUsername() != null ? u.getUsername() : "-"));
                    tvProfileEmail.setText("Email: " + (u.getEmail() != null ? u.getEmail() : "-"));
                    tvProfileCoins.setText("Coins: " + u.getCoins());
                } else {
                    tvProfileEmail.setText("Email: (error " + response.code() + ")");
                    tvProfileCoins.setText("Coins: -");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.android_proyecto.Models.User> call, Throwable t) {
                tvProfileEmail.setText("Email: (error conexión)");
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

        Call<ResponseBody> call = api.logout(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                session.clear();
                Toast.makeText(MenuActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                goToMain();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                session.clear();
                Toast.makeText(MenuActivity.this, "Logged out (connection error: " + t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                goToMain();
            }
        });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción es permanente. ¿Seguro que quieres eliminar tu cuenta?")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Eliminar", (dialog, which) -> doDeleteAccount())
                .show();
    }

    private void doDeleteAccount() {
        String token = session.getToken();

        if (token == null) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            session.clear();
            goToMain();
            return;
        }

        btnDeleteAccount.setEnabled(false);

        api.deleteMe(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnDeleteAccount.setEnabled(true);

                if (response.isSuccessful()) {
                    session.clear();
                    Toast.makeText(MenuActivity.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                    goToMain();
                } else {
                    Toast.makeText(MenuActivity.this, "No se pudo eliminar (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnDeleteAccount.setEnabled(true);
                Toast.makeText(MenuActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
