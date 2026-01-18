package com.example.android_proyecto.Activities;

import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Adapters.TeamMembersAdapter;
import com.example.android_proyecto.Models.TeamResponse;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;
import com.example.android_proyecto.glide.SvgSoftwareLayerSetter;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Response;

public class GroupMembersActivity extends AppCompatActivity {

    private ApiService api;
    private RecyclerView recyclerMembers;

    private ImageView ivTeamAvatar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        String teamName = getIntent().getStringExtra("teamName");

        ivTeamAvatar = findViewById(R.id.ivTeamAvatar);

        TextView tvTitle = findViewById(R.id.tvGroupMembersTitle);
        tvTitle.setText(teamName != null ? teamName : "Members");

        showTeamAvatarFromCache(teamName);

        Button btnBack = findViewById(R.id.btnBackMembers);
        btnBack.setOnClickListener(v -> finish());

        recyclerMembers = findViewById(R.id.recyclerMembers);
        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));

        if (teamName == null || teamName.trim().isEmpty()) {
            recyclerMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
            Toast.makeText(this, "No team name", Toast.LENGTH_SHORT).show();
            return;
        }

        loadMembersPreferInfo(teamName);
    }

    private void showTeamAvatarFromCache(String teamName) {
        if (ivTeamAvatar == null) return;

        String url = session.getTeamAvatarUrl(teamName);
        if (url == null || url.trim().isEmpty()) {
            ivTeamAvatar.setVisibility(android.view.View.GONE);
            return;
        }

        ivTeamAvatar.setVisibility(android.view.View.VISIBLE);
        Glide.with(this).clear(ivTeamAvatar);
        ivTeamAvatar.setImageDrawable(null);

        String u = url.trim().toLowerCase();
        if (u.contains("/svg") || u.contains("svg?") || u.endsWith(".svg")) {
            Glide.with(this)
                    .as(PictureDrawable.class)
                    .load(url)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .listener(new SvgSoftwareLayerSetter())
                    .into(ivTeamAvatar);
        } else {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .centerCrop()
                    .into(ivTeamAvatar);
        }
    }

    private void loadMembersPreferInfo(String teamName) {
        api.getTeamInfo(teamName).enqueue(new retrofit2.Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                TeamResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getMembers() != null) {
                    recyclerMembers.setAdapter(new TeamMembersAdapter(body.getMembers()));
                } else {
                    loadMembersFallbackTeams(teamName);
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                loadMembersFallbackTeams(teamName);
            }
        });
    }

    private void loadMembersFallbackTeams(String teamName) {
        api.getTeam(teamName).enqueue(new retrofit2.Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                TeamResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getMembers() != null) {
                    recyclerMembers.setAdapter(new TeamMembersAdapter(body.getMembers()));
                } else {
                    loadMembersFallbackMe(teamName);
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                loadMembersFallbackMe(teamName);
            }
        });
    }

    private void loadMembersFallbackMe(String teamName) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            recyclerMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
            Toast.makeText(this, "No token", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getTeamMembersMe(token, teamName).enqueue(new retrofit2.Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                TeamResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getMembers() != null) {
                    recyclerMembers.setAdapter(new TeamMembersAdapter(body.getMembers()));
                } else {
                    recyclerMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
                    Toast.makeText(GroupMembersActivity.this,
                            "Could not load members (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                recyclerMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
                Toast.makeText(GroupMembersActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
