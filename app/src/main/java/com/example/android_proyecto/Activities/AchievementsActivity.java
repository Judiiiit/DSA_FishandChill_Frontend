package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.AchievementsAdapter;
import com.example.android_proyecto.Models.AchievementItem;
import com.example.android_proyecto.R;
import com.example.android_proyecto.Services.AchievementsManager;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private AchievementsAdapter adapter;
    private TextView tvAchievementsProgress;
    private ProgressBar progressAchievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        Button btnBack = findViewById(R.id.btnBackAchievements);
        btnBack.setOnClickListener(v -> finish());

        tvAchievementsProgress = findViewById(R.id.tvAchievementsProgress);
        progressAchievements = findViewById(R.id.progressAchievements);

        RecyclerView rv = findViewById(R.id.rvAchievements);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementsAdapter();
        rv.setAdapter(adapter);

        loadAchievements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAchievements();
    }

    private void loadAchievements() {
        AchievementsManager am = new AchievementsManager(this);
        List<AchievementItem> items = am.getAll();

        adapter.setItems(items);

        int total = items != null ? items.size() : 0;
        int unlocked = 0;

        if (items != null) {
            for (AchievementItem it : items) {
                if (it != null && it.isUnlocked()) unlocked++;
            }
        }

        tvAchievementsProgress.setText("Progress: " + unlocked + "/" + total);

        int pct = 0;
        if (total > 0) pct = (int) ((unlocked * 100f) / total);
        progressAchievements.setProgress(pct);
    }
}
