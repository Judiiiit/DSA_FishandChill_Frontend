package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.LeaderboardAdapter;
import com.example.android_proyecto.Models.LeaderboardEntry;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private ProgressBar progressLeaderboard;
    private LeaderboardAdapter adapter;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        progressLeaderboard = findViewById(R.id.progressLeaderboard);

        adapter = new LeaderboardAdapter();
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);

        api = RetrofitClient.getApiService();

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        progressLeaderboard.setVisibility(View.VISIBLE);

        api.getFishLeaderboard(50).enqueue(new Callback<List<LeaderboardEntry>>() {
            @Override
            public void onResponse(Call<List<LeaderboardEntry>> call, Response<List<LeaderboardEntry>> response) {
                progressLeaderboard.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(LeaderboardActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LeaderboardEntry>> call, Throwable t) {
                progressLeaderboard.setVisibility(View.GONE);
                Toast.makeText(LeaderboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
