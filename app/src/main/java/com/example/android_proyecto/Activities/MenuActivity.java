package com.example.android_proyecto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.R;

public class MenuActivity extends AppCompatActivity {

    private Button btnGoGame, btnGoShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnGoGame = findViewById(R.id.btnGoGame);
        btnGoShop = findViewById(R.id.btnGoShop);

        // Botón juego: solo mensaje "en producción"
        btnGoGame.setOnClickListener(v ->
                Toast.makeText(MenuActivity.this,
                        "Feature in production",
                        Toast.LENGTH_SHORT).show()
        );

        // Botón shop: abrir ShopActivity
        btnGoShop.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ShopActivity.class);
            startActivity(intent);
        });
    }
}
