package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.Models.QuestionRequest;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AskQuestionActivity extends AppCompatActivity {

    private EditText etTitle, etMessage, etSender;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        etSender = findViewById(R.id.etSender);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> sendQuestion());
    }

    private void sendQuestion() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String sender = etSender.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty() || sender.isEmpty()) {
            Toast.makeText(this, "Fill title, message and sender", Toast.LENGTH_SHORT).show();
            return;
        }

        // date = null -> el backend la rellena si no viene
        QuestionRequest req = new QuestionRequest(null, title, message, sender);

        ApiService api = RetrofitClient.getApiService();
        api.postQuestion(req).enqueue(new Callback<QuestionRequest>() {
            @Override
            public void onResponse(Call<QuestionRequest> call, Response<QuestionRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AskQuestionActivity.this, "Question sent!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AskQuestionActivity.this,
                            "Error sending question (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuestionRequest> call, Throwable t) {
                Toast.makeText(AskQuestionActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
