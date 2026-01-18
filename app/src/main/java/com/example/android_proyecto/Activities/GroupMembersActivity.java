package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.TeamMembersAdapter;
import com.example.android_proyecto.Models.TeamResponse;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GroupMembersActivity extends AppCompatActivity {

    private ApiService api;
    private RecyclerView recyclerMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        api = RetrofitClient.getApiService();

        String teamName = getIntent().getStringExtra("teamName");

        TextView tvTitle = findViewById(R.id.tvGroupMembersTitle);
        tvTitle.setText(teamName != null ? teamName : "Members");

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
        // Necesitas token aquí. Si no lo tienes en esta activity, usa SessionManager.
        com.example.android_proyecto.Services.SessionManager session =
                new com.example.android_proyecto.Services.SessionManager(this);

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
