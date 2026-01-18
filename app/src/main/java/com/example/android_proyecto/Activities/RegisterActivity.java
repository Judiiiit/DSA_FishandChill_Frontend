package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.MainActivity;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.Models.UserRegister;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ApiService api;

    private EditText etUser, etPass, etPassConfirm, etEmail;
    private ProgressBar progress;
    private TextView tvMsg;
    private Button btnRegister, btnBack;

    // Requirements panel
    private TextView tvReqLen, tvReqLetter, tvReqNumber, tvReqSpecial, tvReqMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUser = findViewById(R.id.etUserReg);
        etEmail = findViewById(R.id.etEmailReg);
        etPass = findViewById(R.id.etPassReg);
        etPassConfirm = findViewById(R.id.etPassConfirmReg);

        progress = findViewById(R.id.progressRegister);
        tvMsg = findViewById(R.id.tvMsgRegister);

        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        // Requirements TextViews
        tvReqLen = findViewById(R.id.tvReqLen);
        tvReqLetter = findViewById(R.id.tvReqLetter);
        tvReqNumber = findViewById(R.id.tvReqNumber);
        tvReqSpecial = findViewById(R.id.tvReqSpecial);
        tvReqMatch = findViewById(R.id.tvReqMatch);

        api = RetrofitClient.getApiService();

        // Live update of requirements
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                updateRequirementsPanel();
            }
        };

        etPass.addTextChangedListener(watcher);
        etPassConfirm.addTextChangedListener(watcher);
        updateRequirementsPanel();

        btnRegister.setOnClickListener(v -> doRegister());

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        btnBack.setEnabled(!show);
    }

    private void doRegister() {
        String username = etUser.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString();
        String password2 = etPassConfirm.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            tvMsg.setText("Please fill in all fields");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvMsg.setText("Invalid email format");
            return;
        }

        String passwordError = getPasswordValidationError(password);
        if (passwordError != null) {
            tvMsg.setText(passwordError);
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

    private String getPasswordValidationError(String password) {
        if (password.length() < 6) return "Password must be at least 6 characters";
        if (!password.matches(".*\\p{L}.*")) return "Password must contain at least one letter";
        if (!password.matches(".*\\p{N}.*")) return "Password must contain at least one number";
        if (!password.matches(".*[^\\p{L}\\p{N}\\s].*")) return "Password must contain at least one symbol";
        return null;
    }

    private void updateRequirementsPanel() {
        String password = etPass.getText().toString();
        String confirm = etPassConfirm.getText().toString();

        setReq(tvReqLen, password.length() >= 6, "At least 6 characters");
        setReq(tvReqLetter, password.matches(".*\\p{L}.*"), "Contains a letter");
        setReq(tvReqNumber, password.matches(".*\\p{N}.*"), "Contains a number");
        setReq(tvReqSpecial, password.matches(".*[^\\p{L}\\p{N}\\s].*"), "Contains a symbol");
        setReq(tvReqMatch, !password.isEmpty() && password.equals(confirm), "Passwords match");
    }

    private void setReq(TextView tv, boolean ok, String label) {
        tv.setText((ok ? "✅ " : "❌ ") + label);
    }
}
