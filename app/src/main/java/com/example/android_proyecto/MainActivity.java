package com.example.android_proyecto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.Activities.FaqActivity;
import com.example.android_proyecto.Activities.LogInActivity;
import com.example.android_proyecto.Activities.RegisterActivity;
import com.example.android_proyecto.Activities.VideoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGoLogin = findViewById(R.id.btnGoLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);
        Button btnFaq = findViewById(R.id.btnFaq);
        Button btnVideo = findViewById(R.id.btnVideo);

        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LogInActivity.class)));

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        btnFaq.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FaqActivity.class)));

        btnVideo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VideoActivity.class)));
    }
}
