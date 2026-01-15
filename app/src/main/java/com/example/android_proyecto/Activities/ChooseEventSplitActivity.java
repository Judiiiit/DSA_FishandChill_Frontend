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

public class ChooseEventSplitActivity extends AppCompatActivity {

    private static final long EVENT_ROTATION_MS = 10 * 60 * 1000L; // 10 min

    private FrameLayout activeEvent;
    private ImageView imgActive;
    private TextView tvEventInfo;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private static class RotatingEvent {
        final String id;
        final String name;
        final int drawableRes;

        RotatingEvent(String id, String name, int drawableRes) {
            this.id = id;
            this.name = name;
            this.drawableRes = drawableRes;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_event_split);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("CURRENT EVENT");

        activeEvent = findViewById(R.id.activeEvent);
        imgActive = findViewById(R.id.imgActive);
        tvEventInfo = findViewById(R.id.tvEventInfo);

        activeEvent.setOnClickListener(v -> openActiveEvent());

        updateUI();
    }

    private RotatingEvent getActiveEvent(long nowMs) {
        long slot = nowMs / EVENT_ROTATION_MS;
        boolean first = (slot % 2 == 0);

        if (first) {
            return new RotatingEvent("1", "Fishing Storm", R.drawable.event_fishing_storm);
        } else {
            return new RotatingEvent("2", "Meteor Arrival", R.drawable.event_meteor_arrival);
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
        imgActive.setContentDescription(ev.name);

        tvEventInfo.setText("Active: " + ev.name + "\nNext in: " + formatMMSS(remaining));
    }

    private void startTicker() {
        if (runnable != null) return;

        runnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopTicker() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    private void openActiveEvent() {
        long now = System.currentTimeMillis();
        RotatingEvent ev = getActiveEvent(now);

        Intent i = new Intent(this, EventUsersActivity.class);
        i.putExtra("eventId", ev.id);
        i.putExtra("eventName", ev.name);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
        startTicker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicker();
    }
}
