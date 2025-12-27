package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.R;

public class ChooseEventSplitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_event_split);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Choose event");

        findViewById(R.id.leftEvent).setOnClickListener(v ->
                openEvent("1", "Fishing Storm")
        );

        findViewById(R.id.rightEvent).setOnClickListener(v ->
                openEvent("2", "Meteor Arrival")
        );
    }

    private void openEvent(String eventId, String eventName) {
        Intent i = new Intent(this, EventUsersActivity.class);
        i.putExtra("eventId", eventId);
        i.putExtra("eventName", eventName);
        startActivity(i);
    }
}
