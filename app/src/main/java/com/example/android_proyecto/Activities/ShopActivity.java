package com.example.android_proyecto.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_proyecto.Adapters.RodsAdapter;
import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;
import com.example.android_proyecto.Services.SessionManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {

    private TextView tvCoins;
    private RecyclerView rvRods;
    private ProgressBar progress;
    private Button btnBack;

    // 🔹 NUEVOS CAMPOS QUE USAMOS DEL LAYOUT
    private Button btnRods;
    private TextView tvInitialMessage;

    private RodsAdapter adapter;
    private ApiService api;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // VISTAS
        tvCoins          = findViewById(R.id.tvCoins);
        rvRods           = findViewById(R.id.rvRods);
        progress         = findViewById(R.id.progressShop);
        btnBack          = findViewById(R.id.btnBack);
        btnRods          = findViewById(R.id.btnRods);              // ⬅️ botón "Rods"
        tvInitialMessage = findViewById(R.id.tvInitialMessage);     // ⬅️ texto "Select a category..."

        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        // RECYCLER Y ADAPTER
        adapter = new RodsAdapter(new ArrayList<>(), this::onBuyRodClicked);
        rvRods.setLayoutManager(new LinearLayoutManager(this));
        rvRods.setAdapter(adapter);

        // Al entrar, solo cargamos el saldo
        loadBalance();

        // Estado inicial: mostrar mensaje, ocultar lista
        rvRods.setVisibility(View.GONE);
        tvInitialMessage.setVisibility(View.VISIBLE);

        // Botón volver
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        // 🔹 CUANDO PULSAS RODS -> MOSTRAR LISTA Y CARGAR CAÑAS
        btnRods.setOnClickListener(v -> {
            tvInitialMessage.setVisibility(View.GONE); // ocultar mensaje inicial
            rvRods.setVisibility(View.VISIBLE);        // mostrar RecyclerView
            loadRods();                                // pedir cañas a la API
        });
    }

    // --- BALANCE / COINS ---

    private void loadBalance() {
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            tvCoins.setText("Coins: --");
            return;
        }

        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int coins = response.body().getCoins();
                    tvCoins.setText("Coins: " + coins);
                } else {
                    tvCoins.setText("Coins: ?");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvCoins.setText("Coins: Error");
                Toast.makeText(ShopActivity.this, "Error loading balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- CARGAR CAÑAS (RODS) ---

    private void loadRods() {
        progress.setVisibility(View.VISIBLE);

        api.getRods().enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<FishingRod> rods = response.body();
                    adapter.setRods(rods);

                    if (rods.isEmpty()) {
                        Toast.makeText(ShopActivity.this, "The shop is empty.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShopActivity.this, "Error loading shop. Code: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- COMPRAR CAÑA ---

    private void onBuyRodClicked(FishingRod rod) {
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No active session. Please log in.", Toast.LENGTH_LONG).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        api.buyRod(token, rod.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "You purchased: " + rod.getName() + "!", Toast.LENGTH_SHORT).show();
                    loadBalance();
                    return;
                }

                String mensaje = "Purchase error. Code: " + response.code();

                try {
                    ResponseBody errorBody = response.errorBody();
                    String errorText = errorBody != null ? errorBody.string() : "";

                    if (errorText.contains("coins")) {
                        mensaje = "Not enough coins.";
                    } else if (errorText.contains("owned")) {
                        mensaje = "You already own this rod.";
                    } else if (errorText.contains("found")) {
                        mensaje = "Item no longer available.";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(ShopActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this, "Connection error while buying: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
