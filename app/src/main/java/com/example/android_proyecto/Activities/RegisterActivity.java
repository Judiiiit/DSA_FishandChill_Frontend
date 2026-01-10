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

        EditText etPasswordReg = findViewById(R.id.etPassReg);
        TextView tvPasswordRequirements = findViewById(R.id.tvPasswordRequirements);

        updatePasswordRequirements(tvPasswordRequirements, "");

        etPasswordReg.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updatePasswordRequirements(tvPasswordRequirements, s.toString());
            }
        });


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

    private String getPasswordValidationError(String password) {
        if (password == null) return "La contraseña es obligatoria";

        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        // Al menos 1 letra (incluye acentos/ñ, etc.)
        if (!password.matches(".*\\p{L}.*")) {
            return "La contraseña debe contener al menos 1 letra";
        }
        // Al menos 1 número
        if (!password.matches(".*\\p{N}.*")) {
            return "La contraseña debe contener al menos 1 numero";
        }
        // Al menos 1 carácter especial (no cuenta espacios)
        if (!password.matches(".*[^\\p{L}\\p{N}\\s].*")) {
            return "La contraseña debe contener al menos 1 caracter especial";
        }

        return null;
    }

    private void updatePasswordRequirements(TextView tv, String password) {
        boolean okLen = password != null && password.length() >= 6;
        boolean okLetter = password != null && password.matches(".*\\p{L}.*");
        boolean okNumber = password != null && password.matches(".*\\p{N}.*");
        boolean okSpecial = password != null && password.matches(".*[^\\p{L}\\p{N}\\s].*");

        int green = androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_green_light);
        int red = androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_red_light);

        // Texto compacto en 1 línea (cambia colores por segmento)
        String prefix = "Requisitos: ";
        String sLen = "6+";
        String sep1 = " | ";
        String sLetter = "letra";
        String sep2 = " | ";
        String sNumber = "numero";
        String sep3 = " | ";
        String sSpecial = "especial";

        String full = prefix + sLen + sep1 + sLetter + sep2 + sNumber + sep3 + sSpecial;

        android.text.SpannableString span = new android.text.SpannableString(full);

        int startLen = prefix.length();
        int endLen = startLen + sLen.length();

        int startLetter = endLen + sep1.length();
        int endLetter = startLetter + sLetter.length();

        int startNumber = endLetter + sep2.length();
        int endNumber = startNumber + sNumber.length();

        int startSpecial = endNumber + sep3.length();
        int endSpecial = startSpecial + sSpecial.length();

        span.setSpan(new android.text.style.ForegroundColorSpan(okLen ? green : red),
                startLen, endLen, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        span.setSpan(new android.text.style.ForegroundColorSpan(okLetter ? green : red),
                startLetter, endLetter, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        span.setSpan(new android.text.style.ForegroundColorSpan(okNumber ? green : red),
                startNumber, endNumber, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        span.setSpan(new android.text.style.ForegroundColorSpan(okSpecial ? green : red),
                startSpecial, endSpecial, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv.setText(span);
    }


}
