package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        session = new SessionManager(this);
        api = RetrofitClient.getApiService();

        btnGoGame = findViewById(R.id.btnGoGame);
        btnGoShop = findViewById(R.id.btnGoShop);
        btnLogout = findViewById(R.id.btnLogout);

        String token = session.getToken();
        Toast.makeText(this, "Token: " + token, Toast.LENGTH_LONG).show();

        btnGoGame.setOnClickListener(v ->
                Toast.makeText(MenuActivity.this,
                        "Feature in production",
                        Toast.LENGTH_SHORT).show()
        );

        btnGoShop.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> doLogout());
    }

    private void doLogout() {
        String token = session.getToken();

        // If there is no token, just clear and go to main
        if (token == null) {
            session.clear();
            goToMain();
            return;
        }

        // Call backend logout (this will delete the token from DB)
        Call<ResponseBody> call = api.logout(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Regardless of HTTP code, clear local session
                session.clear();
                Toast.makeText(MenuActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                goToMain();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Even if there is a connection error, we clear local session
                session.clear();
                Toast.makeText(MenuActivity.this,
                        "Logged out (connection error: " + t.getMessage() + ")",
                        Toast.LENGTH_SHORT).show();
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
