package com.example.android_proyecto.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.TeamMembersAdapter;
import com.example.android_proyecto.Adapters.TeamsAdapter;
import com.example.android_proyecto.Models.TeamRanking;
import com.example.android_proyecto.Models.TeamResponse;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.AchievementsManager;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GroupsActivity extends AppCompatActivity {

    private RecyclerView recyclerTeams;
    private RecyclerView recyclerMyTeamMembers;
    private TextView tvMyTeamName;
    private View sectionMyTeam;
    private Button btnLeaveTeam;
    private Button btnCreateTeam;

    private SessionManager session;
    private ApiService api;

    private String currentTeamName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        new AchievementsManager(this).unlock(AchievementsManager.A_OPEN_GROUPS);

        session = new SessionManager(this);
        api = RetrofitClient.getApiService();

        recyclerTeams = findViewById(R.id.recyclerTeams);
        recyclerTeams.setLayoutManager(new LinearLayoutManager(this));

        recyclerMyTeamMembers = findViewById(R.id.recyclerMyTeamMembers);
        recyclerMyTeamMembers.setLayoutManager(new LinearLayoutManager(this));

        tvMyTeamName = findViewById(R.id.tvMyTeamName);
        sectionMyTeam = findViewById(R.id.sectionMyTeam);
        btnLeaveTeam = findViewById(R.id.btnLeaveTeam);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnLeaveTeam.setOnClickListener(v -> leaveTeam());
        btnCreateTeam.setOnClickListener(v -> showCreateTeamDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void refreshAll() {
        loadMyProfileThenTeams();
    }

    private void loadMyProfileThenTeams() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentTeamName = response.body().getTeamName();
                    if (currentTeamName != null) session.saveTeamName(currentTeamName);
                } else {
                    currentTeamName = session.getTeamName();
                }

                updateMyTeamSection();
                loadTeamsRankingPreferInfo();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                currentTeamName = session.getTeamName();
                updateMyTeamSection();
                loadTeamsRankingPreferInfo();
            }
        });
    }

    private boolean hasTeam() {
        return currentTeamName != null
                && !currentTeamName.trim().isEmpty()
                && !"None".equalsIgnoreCase(currentTeamName.trim());
    }

    private void updateMyTeamSection() {
        sectionMyTeam.setVisibility(View.VISIBLE);

        if (!hasTeam()) {
            tvMyTeamName.setText("No team");
            btnLeaveTeam.setVisibility(View.GONE);
            recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
            return;
        }

        tvMyTeamName.setText(currentTeamName);
        btnLeaveTeam.setVisibility(View.VISIBLE);

        loadTeamMembersPreferInfo(currentTeamName);
    }

    private void showCreateTeamDialog() {
        if (hasTeam()) {
            Toast.makeText(this, "Leave your current team first", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(this);
        input.setHint("Team name (no spaces recommended)");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("Create team")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Create", (d, w) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Team name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createTeam(name);
                })
                .show();
    }

    private void createTeam(String teamName) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        api.createTeam(token, teamName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    Toast.makeText(GroupsActivity.this, "Team created!", Toast.LENGTH_SHORT).show();
                    refreshAll();
                    return;
                }

                // IMPORTANT: show real error (code + body)
                String body = readBodySafe(response.errorBody());
                Toast.makeText(GroupsActivity.this,
                        "Create failed (" + response.code() + "): " + body,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeamsRankingPreferInfo() {
        api.getTeamsRankingInfo().enqueue(new retrofit2.Callback<List<TeamRanking>>() {
            @Override
            public void onResponse(Call<List<TeamRanking>> call, Response<List<TeamRanking>> response) {
                // Si el body viene null aunque sea 200, hacemos fallback
                if (response.isSuccessful() && response.body() != null) {
                    bindTeams(response.body());
                } else {
                    loadTeamsRankingFallback();
                }
            }

            @Override
            public void onFailure(Call<List<TeamRanking>> call, Throwable t) {
                loadTeamsRankingFallback();
            }
        });
    }


    private void loadTeamsRankingFallback() {
        api.getTeams().enqueue(new Callback<List<TeamRanking>>() {
            @Override
            public void onResponse(Call<List<TeamRanking>> call, Response<List<TeamRanking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindTeams(response.body());
                } else {
                    String body = readBodySafe(response.errorBody());
                    Toast.makeText(GroupsActivity.this,
                            "Could not load teams (" + response.code() + "): " + body,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<TeamRanking>> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindTeams(List<TeamRanking> teams) {
        TeamsAdapter adapter = new TeamsAdapter(
                teams,
                currentTeamName,
                new TeamsAdapter.OnTeamActionListener() {
                    @Override
                    public void onOpenMembers(TeamRanking team) {
                        openMembers(team.getName());
                    }

                    @Override
                    public void onJoin(TeamRanking team) {
                        joinTeam(team.getName());
                    }
                }
        );
        recyclerTeams.setAdapter(adapter);
    }


    private void loadTeamMembersPreferInfo(String teamName) {
        api.getTeamInfo(teamName).enqueue(new retrofit2.Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                // Si /info devuelve algo no parseable o members=null, hacemos fallback
                TeamResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getMembers() != null) {
                    recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(body.getMembers()));
                } else {
                    loadTeamMembersFallbackTeams(teamName);
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                loadTeamMembersFallbackTeams(teamName);
            }
        });
    }


    private void loadTeamMembersFallback(String teamName) {
        api.getTeam(teamName).enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMembers() != null) {
                    recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(response.body().getMembers()));
                } else {
                    recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
                    String body = readBodySafe(response.errorBody());
                    Toast.makeText(GroupsActivity.this,
                            "Could not load members (" + response.code() + "): " + body,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openMembers(String teamName) {
        Intent i = new Intent(this, GroupMembersActivity.class);
        i.putExtra("teamName", teamName);
        startActivity(i);
    }


    private void joinTeam(String teamName) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasTeam()) {
            Toast.makeText(this, "You are already in a team", Toast.LENGTH_SHORT).show();
            return;
        }

        api.joinTeam(token, teamName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    Toast.makeText(GroupsActivity.this, "Joined team!", Toast.LENGTH_SHORT).show();
                    refreshAll();
                    return;
                }

                String body = readBodySafe(response.errorBody());
                Toast.makeText(GroupsActivity.this,
                        "Join failed (" + response.code() + "): " + body,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveTeam() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        api.leaveTeam(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    session.clearTeamName();
                    currentTeamName = "None";
                    Toast.makeText(GroupsActivity.this, "Left team", Toast.LENGTH_SHORT).show();
                    refreshAll();
                    return;
                }

                String body = readBodySafe(response.errorBody());
                Toast.makeText(GroupsActivity.this,
                        "Leave failed (" + response.code() + "): " + body,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String readBodySafe(ResponseBody body) {
        if (body == null) return "";
        try {
            return body.string();
        } catch (IOException e) {
            return "";
        }
    }

    private void loadTeamMembersFallbackTeams(String teamName) {
        api.getTeam(teamName).enqueue(new retrofit2.Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                TeamResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getMembers() != null) {
                    recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(body.getMembers()));
                } else {
                    loadTeamMembersFallbackMe(teamName);
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                loadTeamMembersFallbackMe(teamName);
            }
        });
    }

    private void loadTeamMembersFallbackMe(String teamName) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
            return;
        }

        api.getTeamMembersMe(token, teamName).enqueue(new retrofit2.Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                TeamResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getMembers() != null) {
                    recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(body.getMembers()));
                } else {
                    recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
                    Toast.makeText(GroupsActivity.this,
                            "Could not load members (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                recyclerMyTeamMembers.setAdapter(new TeamMembersAdapter(Collections.emptyList()));
                Toast.makeText(GroupsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
