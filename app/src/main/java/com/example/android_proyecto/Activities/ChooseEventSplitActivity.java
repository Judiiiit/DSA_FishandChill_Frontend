package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.R;
import com.example.android_proyecto.Services.AchievementsManager;

public class ChooseEventSplitActivity extends AppCompatActivity {

    private static final long EVENT_ROTATION_MS = 10 * 60 * 1000L;

    private FrameLayout activeEvent;
    private ImageView imgActive;
    private TextView tvEventInfo;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private static class RotatingEvent {
        final int id;
        final String name;
        final int drawableRes;

        RotatingEvent(int id, String name, int drawableRes) {
            this.id = id;
            this.name = name;
            this.drawableRes = drawableRes;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_event_split);

        new AchievementsManager(this).unlock(AchievementsManager.A_OPEN_EVENTS);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("CURRENT EVENT");

        activeEvent = findViewById(R.id.activeEvent);
        imgActive = findViewById(R.id.imgActive);
        tvEventInfo = findViewById(R.id.tvEventInfo);

        activeEvent.setOnClickListener(v -> openEvent());

        updateUI();
    }

    private RotatingEvent getActiveEvent(long nowMs) {
        long slot = nowMs / EVENT_ROTATION_MS;
        boolean first = (slot % 2 == 0);

        if (first) {
            return new RotatingEvent(1, "Fishing Storm", R.drawable.event_fishing_storm);
        } else {
            return new RotatingEvent(2, "Meteor Arrival", R.drawable.event_meteor_arrival);
        }
    }

    private long getMillisUntilNextRotation(long nowMs) {
        long inSlot = nowMs % EVENT_ROTATION_MS;
        return EVENT_ROTATION_MS - inSlot;
    }

    private String formatMMSS(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void updateUI() {
        long now = System.currentTimeMillis();
        RotatingEvent ev = getActiveEvent(now);
        long remaining = getMillisUntilNextRotation(now);

        imgActive.setImageResource(ev.drawableRes);
        tvEventInfo.setText("Active: " + ev.name + "\nNext in: " + formatMMSS(remaining));
    }

    private void openEvent() {
        RotatingEvent ev = getActiveEvent(System.currentTimeMillis());
        Intent i = new Intent(this, EventUsersActivity.class);
        i.putExtra("eventId", String.valueOf(ev.id));
        i.putExtra("eventName", ev.name);
        startActivity(i);
    }

    private void startTicker() {
        runnable = () -> {
            updateUI();
            handler.postDelayed(runnable, 1000);
        };
        handler.post(runnable);
    }

    private void stopTicker() {
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicker();
    }
}
