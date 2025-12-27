package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Adapters.EventUserAdapter;
import com.example.android_proyecto.Models.EventUser;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_users);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) eventId = "1";

        String eventName = getIntent().getStringExtra("eventName");
        if (eventName == null) eventName = "Event " + eventId;

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Registered users - " + eventName);

        loadUsers(eventId);
    }

    private void loadUsers(String eventId) {
        ApiService api = RetrofitClient.getApiService();

        api.getEventUsers(eventId).enqueue(new Callback<List<EventUser>>() {
            @Override
            public void onResponse(Call<List<EventUser>> call, Response<List<EventUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerUsers.setAdapter(new EventUserAdapter(response.body()));
                } else {
                    Toast.makeText(EventUsersActivity.this,
                            "Could not load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventUser>> call, Throwable t) {
                Toast.makeText(EventUsersActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
