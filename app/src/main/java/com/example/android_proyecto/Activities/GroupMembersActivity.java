package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.MembersAdapter;
import com.example.android_proyecto.Models.GroupUser;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMembersActivity extends AppCompatActivity {

    private SessionManager session;
    private ApiService api;
    private RecyclerView recyclerMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        session = new SessionManager(this);
        api = RetrofitClient.getApiService();

        int groupId = getIntent().getIntExtra("groupId", -1);
        String groupName = getIntent().getStringExtra("groupName");

        TextView tvTitle = findViewById(R.id.tvGroupMembersTitle);
        tvTitle.setText(groupName != null ? groupName : "Members");

        Button btnBack = findViewById(R.id.btnBackMembers);
        btnBack.setOnClickListener(v -> finish());

        recyclerMembers = findViewById(R.id.recyclerMembers);
        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));

        loadMembers(groupId);
    }

    private void loadMembers(int groupId) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getGroupUsers(token, groupId).enqueue(new Callback<List<GroupUser>>() {
            @Override
            public void onResponse(Call<List<GroupUser>> call, Response<List<GroupUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerMembers.setAdapter(new MembersAdapter(response.body()));
                } else {
                    Toast.makeText(GroupMembersActivity.this, "Could not load members (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GroupUser>> call, Throwable t) {
                Toast.makeText(GroupMembersActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
