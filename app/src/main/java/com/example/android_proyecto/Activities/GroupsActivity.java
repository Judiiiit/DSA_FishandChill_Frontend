package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.GroupsAdapter;
import com.example.android_proyecto.Models.Group;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.AchievementsManager;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsActivity extends AppCompatActivity {

    private RecyclerView recyclerGroups;
    private SessionManager session;

    private final Set<Integer> joinedGroupIds = new HashSet<>();
    private GroupsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        new AchievementsManager(this).unlock(AchievementsManager.A_OPEN_GROUPS);

        session = new SessionManager(this);

        recyclerGroups = findViewById(R.id.recyclerGroups);
        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        joinedGroupIds.clear();
        joinedGroupIds.addAll(session.getJoinedGroups());
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void loadGroups() {
        ApiService api = RetrofitClient.getApiService();

        api.getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    joinedGroupIds.clear();
                    joinedGroupIds.addAll(session.getJoinedGroups());

                    adapter = new GroupsAdapter(
                            response.body(),
                            joinedGroupIds,
                            new GroupsAdapter.OnGroupActionListener() {
                                @Override
                                public void onOpenMembers(Group group) {
                                    openMembers(group.getId(), group.getName());
                                }

                                @Override
                                public void onJoin(Group group) {
                                    joinGroup(group.getId());
                                }
                            }
                    );

                    recyclerGroups.setAdapter(adapter);

                } else {
                    Toast.makeText(GroupsActivity.this, "Could not load groups", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMembers(int groupId, String groupName) {
        Intent i = new Intent(this, GroupMembersActivity.class);
        i.putExtra("groupId", groupId);
        i.putExtra("groupName", groupName);
        startActivity(i);
    }

    private void joinGroup(int groupId) {
        ApiService api = RetrofitClient.getApiService();

        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentGroup = session.getCurrentGroupId();
        if (currentGroup != -1 && currentGroup != groupId) {
            Toast.makeText(this, "You are already in another group", Toast.LENGTH_SHORT).show();
            return;
        }

        api.joinGroup(token, groupId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.code() == 200) {
                    session.saveCurrentGroupId(groupId);
                    session.addJoinedGroup(groupId);

                    joinedGroupIds.add(groupId);
                    if (adapter != null) adapter.notifyDataSetChanged();

                    Toast.makeText(GroupsActivity.this, "Joined group!", Toast.LENGTH_SHORT).show();

                } else if (response.code() == 409) {
                    Toast.makeText(GroupsActivity.this, "Already in a group", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(GroupsActivity.this, "Unauthorized", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(GroupsActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupsActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GroupsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
