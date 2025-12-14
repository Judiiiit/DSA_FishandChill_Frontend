package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.FaqAdapter;
import com.example.android_proyecto.Models.Faq;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaqActivity extends AppCompatActivity {

    private RecyclerView recyclerFaqs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        recyclerFaqs = findViewById(R.id.recyclerFaqs);
        recyclerFaqs.setLayoutManager(new LinearLayoutManager(this));

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


        Button btnAskQuestion = findViewById(R.id.btnAskQuestion);
        btnAskQuestion.setOnClickListener(v ->
                startActivity(new Intent(FaqActivity.this, AskQuestionActivity.class))
        );

        loadFaqs();
    }

    private void loadFaqs() {
        ApiService api = RetrofitClient.getApiService();

        api.getFaqs().enqueue(new Callback<List<Faq>>() {
            @Override
            public void onResponse(Call<List<Faq>> call, Response<List<Faq>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FaqAdapter adapter = new FaqAdapter(response.body());
                    recyclerFaqs.setAdapter(adapter);
                } else {
                    Toast.makeText(FaqActivity.this,
                            "Could not load FAQs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Faq>> call, Throwable t) {
                Toast.makeText(FaqActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
