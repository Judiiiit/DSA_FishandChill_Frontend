package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.Models.Token;
import com.example.android_proyecto.Models.UserLogIn;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;
import com.example.android_proyecto.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private ApiService api;
    private SessionManager session;

    private EditText etUser, etPass;
    private ProgressBar progress;
    private TextView tvMsg;
    private Button btnLogin, btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        progress = findViewById(R.id.progressLogin);
        tvMsg = findViewById(R.id.tvMsgLogin);
        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        btnLogin.setOnClickListener(v -> doLogin());

        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(LogInActivity.this, RegisterActivity.class)));
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
                    tvMsg.setText("Login successful");

                    startActivity(new Intent(LogInActivity.this, MainActivity.class));
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
