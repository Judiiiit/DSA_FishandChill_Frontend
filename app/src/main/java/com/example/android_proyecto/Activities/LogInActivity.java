package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.Models.Token;
import com.example.android_proyecto.Models.UserLogIn;
import com.example.android_proyecto.R;
import com.example.android_proyecto.MainActivity;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private ApiService api;
    private SessionManager session;

    private EditText etUser, etPass;
    private ProgressBar progress;
    private TextView tvMsg;
    private Button btnLogin, btnCreateAccount, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progressLogin);
        tvMsg = findViewById(R.id.tvMsgLogin);
        btnBack = findViewById(R.id.btnBack);



        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        String existingToken = session.getToken();
        if (existingToken != null) {
            startActivity(new Intent(LogInActivity.this, MenuActivity.class));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void doLogin() {
        String username = etUser.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            tvMsg.setText("Please enter username and password");
            return;
        }

        showLoading(true);
        tvMsg.setText("");

        Call<Token> call = api.login(new UserLogIn(username, password));
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Token token = response.body();
                    session.saveToken(token.getToken()); // Guardamos el token en SessionManager
                    session.saveUsername(username);
                    Toast.makeText(LogInActivity.this,
                            "Login successful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LogInActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (response.code() == 401) {
                    tvMsg.setText("Invalid username or password");
                }
                else {
                    tvMsg.setText("Login error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                showLoading(false);
                tvMsg.setText("Connection error: " + t.getMessage());
            }
        });
    }
}
