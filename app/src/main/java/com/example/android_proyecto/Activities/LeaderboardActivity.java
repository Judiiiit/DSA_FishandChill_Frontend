package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Adapters.LeaderboardAdapter;
import com.example.android_proyecto.Models.LeaderboardEntry;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.AchievementsManager;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private ProgressBar progressLeaderboard;
    private Button btnBackLeaderboard;

    private TextView tvTop1Name, tvTop1Value;
    private TextView tvTop2Name, tvTop2Value;
    private TextView tvTop3Name, tvTop3Value;
    private ImageView imgTop1Avatar;
    private ImageView imgTop2Avatar;
    private ImageView imgTop3Avatar;
    private TextView tvMyRank;

    private LeaderboardAdapter adapter;
    private ApiService api;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        new AchievementsManager(this).unlock(AchievementsManager.A_OPEN_LEADERBOARD);

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        progressLeaderboard = findViewById(R.id.progressLeaderboard);
        btnBackLeaderboard = findViewById(R.id.btnBackLeaderboard);

        tvTop1Name = findViewById(R.id.tvTop1Name);
        tvTop1Value = findViewById(R.id.tvTop1Value);
        tvTop2Name = findViewById(R.id.tvTop2Name);
        tvTop2Value = findViewById(R.id.tvTop2Value);
        tvTop3Name = findViewById(R.id.tvTop3Name);
        tvTop3Value = findViewById(R.id.tvTop3Value);
        imgTop1Avatar = findViewById(R.id.imgTop1Avatar);
        imgTop2Avatar = findViewById(R.id.imgTop2Avatar);
        imgTop3Avatar = findViewById(R.id.imgTop3Avatar);

        tvMyRank = findViewById(R.id.tvMyRank);

        btnBackLeaderboard.setOnClickListener(v -> finish());

        adapter = new LeaderboardAdapter();
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);

        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        progressLeaderboard.setVisibility(View.VISIBLE);

        api.getFishLeaderboard().enqueue(new Callback<List<LeaderboardEntry>>() {
            @Override
            public void onResponse(Call<List<LeaderboardEntry>> call, Response<List<LeaderboardEntry>> response) {
                progressLeaderboard.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    bindLeaderboard(response.body());
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

    private void bindLeaderboard(List<LeaderboardEntry> list) {
        if (list == null) list = new ArrayList<>();

        if (list.size() > 0) {
            LeaderboardEntry e = list.get(0);
            tvTop1Name.setText(e.getUsername());
            tvTop1Value.setText(String.valueOf(e.getTotalFishes()));
            Glide.with(this)
                    .load(e.getAvatarUrl())
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .circleCrop()
                    .into(imgTop1Avatar);
        } else {
            tvTop1Name.setText("---");
            tvTop1Value.setText("0");
            imgTop1Avatar.setImageResource(R.drawable.avatar_1);
        }

        if (list.size() > 1) {
            LeaderboardEntry e = list.get(1);
            tvTop2Name.setText(e.getUsername());
            tvTop2Value.setText(String.valueOf(e.getTotalFishes()));
            Glide.with(this)
                    .load(e.getAvatarUrl())
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .circleCrop()
                    .into(imgTop2Avatar);
        } else {
            tvTop2Name.setText("---");
            tvTop2Value.setText("0");
            imgTop2Avatar.setImageResource(R.drawable.avatar_1);
        }

        if (list.size() > 2) {
            LeaderboardEntry e = list.get(2);
            tvTop3Name.setText(e.getUsername());
            tvTop3Value.setText(String.valueOf(e.getTotalFishes()));
            Glide.with(this)
                    .load(e.getAvatarUrl())
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .circleCrop()
                    .into(imgTop3Avatar);
        } else {
            tvTop3Name.setText("---");
            tvTop3Value.setText("0");
            imgTop3Avatar.setImageResource(R.drawable.avatar_1);
        }

        List<LeaderboardEntry> rest = new ArrayList<>();
        if (list.size() > 3) {
            rest.addAll(list.subList(3, list.size()));
        }
        adapter.setItems(rest);

        String myUser = session.getUsername();
        int myIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            LeaderboardEntry e = list.get(i);
            if (e != null && e.getUsername() != null && e.getUsername().equalsIgnoreCase(myUser)) {
                myIndex = i;
                break;
            }
        }

        if (myIndex >= 0) {
            LeaderboardEntry me = list.get(myIndex);
            tvMyRank.setText("You: #" + (myIndex + 1) + "  " + me.getTotalFishes() + " fishes");
            tvMyRank.setVisibility(View.VISIBLE);
        } else {
            tvMyRank.setText("You: not ranked");
            tvMyRank.setVisibility(View.VISIBLE);
        }
    }
}
