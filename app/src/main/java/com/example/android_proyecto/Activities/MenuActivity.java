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

        String username = session.getUsername();
        tvWelcomeUser.setText("Welcome, " + username + "!");

        String token = session.getToken();

        unityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // si Unity te devuelve algo
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
    }

    private void closeSettings() {
        findViewById(R.id.frameLayout2).setVisibility(View.VISIBLE);
        settingsPanel.setVisibility(View.GONE);
        btnBackFromSettings.setVisibility(View.GONE);
        btnSettings.setVisibility(View.VISIBLE);
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

    private void goToMain() {
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
