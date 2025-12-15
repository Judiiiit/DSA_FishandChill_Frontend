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

import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.Models.UserRegister;
import com.example.android_proyecto.R;
import com.example.android_proyecto.MainActivity;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ApiService api;
    private EditText etUser, etPass, etPassConfirm, etEmail;
    private ProgressBar progress;
    private TextView tvMsg;
    private Button btnRegister, btnBack;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUser = findViewById(R.id.etUserReg);
        etPass = findViewById(R.id.etPassReg);
        etPassConfirm = findViewById(R.id.etPassConfirmReg);
        etEmail = findViewById(R.id.etEmailReg);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progressRegister);
        tvMsg = findViewById(R.id.tvMsgRegister);
        api = RetrofitClient.getApiService();
        btnBack = findViewById(R.id.btnBack);

        session = new SessionManager(this);

        btnRegister.setOnClickListener(v -> doRegister());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void doRegister() {
        String username = etUser.getText().toString().trim();
        String password = etPass.getText().toString().trim();
        String password2 = etPassConfirm.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || password2.isEmpty() || email.isEmpty()) {
            tvMsg.setText("Please fill in all fields");
            return;
        }

        if (!email.contains("@")) {
            tvMsg.setText("Invalid email format");
            return;
        }

        if (password.length() < 6) {
            tvMsg.setText("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(password2)) {
            tvMsg.setText("Passwords do not match");
            return;
        }

        showLoading(true);
        tvMsg.setText("");

        Call<User> call = api.register(new UserRegister(username, password, email));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();


                    Toast.makeText(RegisterActivity.this,
                            "Register completed", Toast.LENGTH_LONG).show();

                    startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
                    finish();
                } else if (response.code() == 409) {
                    tvMsg.setText("Username already exists");
                } else {
                    tvMsg.setText("Registration error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoading(false);
                tvMsg.setText("Connection error: " + t.getMessage());
            }
        });
    }
}
