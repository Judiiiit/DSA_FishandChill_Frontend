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
import com.example.android_proyecto.Services.SessionManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private Button btnJoinEvent;
    private int eventId;
    private String eventName;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_users);

        session = new SessionManager(this);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        btnJoinEvent = findViewById(R.id.btnJoinEvent);

        eventId = Integer.parseInt(getIntent().getStringExtra("eventId"));
        eventName = getIntent().getStringExtra("eventName");

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Registered users - " + eventName);

        btnJoinEvent.setOnClickListener(v -> subscribe());

        loadUsers();
    }

    private void loadUsers() {
        ApiService api = RetrofitClient.getApiService();
        String myUser = session.getUsername();

        api.getRegisteredUsersInEvent(eventId).enqueue(new Callback<List<EventUser>>() {
            @Override
            public void onResponse(Call<List<EventUser>> call, Response<List<EventUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventUser> users = response.body();
                    recyclerUsers.setAdapter(new EventUserAdapter(users));

                    boolean joined = false;
                    for (EventUser u : users) {
                        if (u.getUsername().equals(myUser)) {
                            joined = true;
                            break;
                        }
                    }

                    if (joined) {
                        btnJoinEvent.setText("Joined");
                        btnJoinEvent.setEnabled(false);
                    } else {
                        btnJoinEvent.setText("Join");
                        btnJoinEvent.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EventUser>> call, Throwable t) {
                Toast.makeText(EventUsersActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribe() {
        String token = session.getToken();
        ApiService api = RetrofitClient.getApiService();

        api.subscribeToEvent(token, eventId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    loadUsers();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EventUsersActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
