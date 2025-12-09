package com.example.android_proyecto.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {

    private TextView tvCoins;
    private RecyclerView rvRods;
    private ProgressBar progress;
    private Button btnBack;

    // Botón de categoría y mensaje inicial
    private Button btnRods;
    private TextView tvInitialMessage;

    private Button btnInventory;

    private RodsAdapter adapter;
    private ApiService api;
    private SessionManager session;

    private Set<String> ownedRodNames = new HashSet<>();
    private final List<FishingRod> ownedRods = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // VISTAS
        tvCoins          = findViewById(R.id.tvCoins);
        rvRods           = findViewById(R.id.rvRods);
        progress         = findViewById(R.id.progressShop);
        btnBack          = findViewById(R.id.btnBack);
        btnRods          = findViewById(R.id.btnRods);
        btnInventory     = findViewById(R.id.btnInventory);
        tvInitialMessage = findViewById(R.id.tvInitialMessage);

        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        ownedRodNames = session.getOwnedRods();

        adapter = new RodsAdapter(new ArrayList<>(), this::onBuyRodClicked, ownedRodNames);
        rvRods.setLayoutManager(new GridLayoutManager(this, 2));
        rvRods.setAdapter(adapter);

        loadBalance();

        rvRods.setVisibility(View.GONE);
        tvInitialMessage.setVisibility(View.VISIBLE);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        btnRods.setOnClickListener(v -> {
            tvInitialMessage.setVisibility(View.GONE);
            rvRods.setVisibility(View.VISIBLE);
            adapter.setInventoryMode(false);
            adapter.setOwnedRodNames(ownedRodNames);
            loadRods();
        });

        btnInventory.setOnClickListener(v -> {
            tvInitialMessage.setVisibility(View.GONE);
            rvRods.setVisibility(View.VISIBLE);

            adapter.setInventoryMode(true);
            adapter.setOwnedRodNames(ownedRodNames);

            loadInventoryFromServer();
        });
    }


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


    private void loadRods() {
        progress.setVisibility(View.VISIBLE);

        api.getRods().enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<FishingRod> rods = response.body();
                    adapter.setRods(rods);

                    ownedRods.clear();
                    for (FishingRod rod : rods) {
                        if (ownedRodNames.contains(rod.getName())) {
                            ownedRods.add(rod);
                        }
                    }
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

    private void showInventory() {
        if (ownedRods.isEmpty()) {
            Toast.makeText(this, "You haven't bought any rods yet.", Toast.LENGTH_SHORT).show();
            adapter.setRods(new ArrayList<>()); // lista vacía
            return;
        }

        adapter.setRods(new ArrayList<>(ownedRods));
    }

    private void addRodToInventory(FishingRod rod) {
        if (rod == null || rod.getName() == null) return;

        for (FishingRod r : ownedRods) {
            if (rod.getName().equals(r.getName())) {
                return;
            }
        }

        ownedRods.add(rod);

        if (!ownedRodNames.contains(rod.getName())) {
            ownedRodNames.add(rod.getName());
            session.saveOwnedRods(ownedRodNames);
        }
    }

    private void loadInventoryFromServer() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No active session. Please log in.", Toast.LENGTH_LONG).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        api.getMyOwnedFishingRods(token).enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<FishingRod> rods = response.body();

                    ownedRods.clear();
                    ownedRodNames.clear();

                    for (FishingRod rod : rods) {
                        ownedRods.add(rod);
                        if (rod.getName() != null) {
                            ownedRodNames.add(rod.getName());
                        }
                    }

                    session.saveOwnedRods(ownedRodNames);

                    showInventory();
                } else {
                    Toast.makeText(ShopActivity.this,
                            "Error loading inventory. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this,
                        "Connection error while loading inventory: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

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
                    addRodToInventory(rod);
                    adapter.setOwnedRodNames(ownedRodNames);
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
