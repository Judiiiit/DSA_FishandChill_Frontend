package com.example.android_proyecto.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.android_proyecto.Adapters.CapturedFishAdapter;
import com.example.android_proyecto.Models.CapturedFish;
import com.example.android_proyecto.Models.SellCapturedFish;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {

    // UI
    private TextView tvCoins, tvInitialMessage;
    private RecyclerView rvRods, rvFishes;
    private ProgressBar progress;
    private Button btnBack, btnRods, btnInventory, btnFishes;

    // Adapters
    private RodsAdapter rodsAdapter;
    private CapturedFishAdapter fishesAdapter;

    // Services
    private ApiService api;
    private SessionManager session;
    private String token;

    // LOCAL DATA STORAGE
    private List<FishingRod> allRodsList = new ArrayList<>();
    private Set<String> ownedRodNames = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Init Views
        tvCoins = findViewById(R.id.tvCoins);
        tvInitialMessage = findViewById(R.id.tvInitialMessage);
        rvRods = findViewById(R.id.rvRods);
        rvFishes = findViewById(R.id.rvFishes);
        progress = findViewById(R.id.progressShop);
        btnBack = findViewById(R.id.btnBack);
        btnRods = findViewById(R.id.btnRods);
        btnInventory = findViewById(R.id.btnInventory);
        btnFishes = findViewById(R.id.btnFishes);

        // Init Service
        api = RetrofitClient.getApiService();
        session = new SessionManager(this);
        token = session.getToken(); // Only using session to get the token

        // Init Adapter
        rodsAdapter = new RodsAdapter(this::buyRod);
        rvRods.setLayoutManager(new GridLayoutManager(this, 2));
        rvRods.setAdapter(rodsAdapter);

        fishesAdapter = new CapturedFishAdapter((capturedFish, coinsGained) -> sellFishToBackend(capturedFish, coinsGained));
        rvFishes.setLayoutManager(new GridLayoutManager(this, 2));
        rvFishes.setAdapter(fishesAdapter);

        // Listeners
        btnBack.setOnClickListener(v -> finish());

        // Show Shop (All Rods)
        btnRods.setOnClickListener(v -> showShopView());

        // Show Inventory (Owned Rods)
        btnInventory.setOnClickListener(v -> showInventoryView());

        // Show Fishes captured to sell
        btnFishes.setOnClickListener(v -> showFishesView());

        // Initial Load
        loadAllShopData();
    }

    /**
     * Loads Balance, All Rods, and Owned Rods from API
     */
    private void loadAllShopData() {
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        tvInitialMessage.setVisibility(View.GONE);
        rvRods.setVisibility(View.GONE);
        rvFishes.setVisibility(View.GONE);

        // 1. Load Balance
        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvCoins.setText("Coins: " + response.body().getCoins());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) { }
        });

        // 2. Load All Available Rods
        api.getRods().enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRodsList = response.body();

                    // 3. Chain: Load Owned Rods immediately after getting all rods
                    fetchOwnedRods();
                } else {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(ShopActivity.this, "Error loading shop items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOwnedRods() {
        api.getMyOwnedFishingRods(token).enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                progress.setVisibility(View.GONE);
                rvRods.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    ownedRodNames.clear();
                    for (FishingRod rod : response.body()) {
                        ownedRodNames.add(rod.getName());
                    }

                    // Default view is Shop
                    showInitialMessage();
                }
            }

            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                showInitialMessage();
            }
        });
    }

    // --- VIEW LOGIC ---

    private void hideAllContent() {
        rvRods.setVisibility(View.GONE);
        rvFishes.setVisibility(View.GONE);
        tvInitialMessage.setVisibility(View.GONE);
    }

    private void showInitialMessage() {
        hideAllContent();
        tvInitialMessage.setVisibility(View.VISIBLE);
    }

    private void showShopView() {
        hideAllContent();
        rvRods.setVisibility(View.VISIBLE);
        // Pass "All Rods" to adapter
        rodsAdapter.setInventoryMode(false); // Enable buy buttons
        rodsAdapter.setOwnedRodNames(ownedRodNames); // Tell adapter what we own (to grey them out)
        rodsAdapter.setRods(allRodsList);
    }

    private void showInventoryView() {
        hideAllContent();
        rvRods.setVisibility(View.VISIBLE);
        // Filter "All Rods" to find the objects that match our "Owned Names"
        List<FishingRod> myInventory = new ArrayList<>();
        for (FishingRod rod : allRodsList) {
            if (ownedRodNames.contains(rod.getName())) {
                myInventory.add(rod);
            }
        }

        // Pass only owned rods to adapter
        rodsAdapter.setInventoryMode(true); // Hide buy buttons
        rodsAdapter.setRods(myInventory);
    }

    private void showFishesView() {
        hideAllContent();
        rvFishes.setVisibility(View.VISIBLE);
        loadMyFishes();
    }


    // ---------------- FISHES LOAD ----------------

    private void loadMyFishes() {
        progress.setVisibility(View.VISIBLE);
        tvInitialMessage.setVisibility(View.GONE);
        rvRods.setVisibility(View.GONE);
        rvFishes.setVisibility(View.GONE);

        api.getMyCapturedFishes(token).enqueue(new Callback<List<CapturedFish>>() {
            @Override
            public void onResponse(Call<List<CapturedFish>> call, Response<List<CapturedFish>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<CapturedFish> fishes = response.body();

                    if (fishes.isEmpty()) {
                        tvInitialMessage.setVisibility(View.VISIBLE);
                        tvInitialMessage.setText("No fishes to sell.");
                    } else {
                        rvFishes.setVisibility(View.VISIBLE);
                        fishesAdapter.setFishes(fishes);
                    }
                } else {
                    tvInitialMessage.setVisibility(View.VISIBLE);
                    tvInitialMessage.setText("Error loading fishes.");
                }
            }

            @Override
            public void onFailure(Call<List<CapturedFish>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvInitialMessage.setVisibility(View.VISIBLE);
                tvInitialMessage.setText("Network error.");
            }
        });
    }


    // --- BUY LOGIC ---

    private void buyRod(FishingRod rod) {
        progress.setVisibility(View.VISIBLE);

        api.buyRod(token, rod.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "Bought: " + rod.getName(), Toast.LENGTH_SHORT).show();

                    // 1. Update Local Data
                    ownedRodNames.add(rod.getName());

                    // 2. Refresh Adapter (It will see the new name in the Set and disable the button)
                    rodsAdapter.setOwnedRodNames(ownedRodNames);

                    // 3. Update Balance (Optional: could assume price and subtract locally, but safer to fetch)
                    updateBalance();

                } else {
                    Toast.makeText(ShopActivity.this, "Could not buy item. Check coins.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalance() {
        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvCoins.setText("Coins: " + response.body().getCoins());
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    // --- SELL FISH  ---
    private void sellFishToBackend(CapturedFish cf, int price) {

        if (cf == null || cf.getFishSpecies() == null || cf.getFishSpecies().getSpeciesName() == null) {
            Toast.makeText(this, "Fish data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String fishSpeciesName = cf.getFishSpecies().getSpeciesName();

        // Nos llega ahora como: "2025-12-23 17:11:37.0" y lo convertimos al ISO que pide Swagger
        String captureTimeIso = toIsoZulu(cf.getCaptureTime());

        if (captureTimeIso == null) {
            Toast.makeText(this, "Invalid captureTime", Toast.LENGTH_SHORT).show();
            return;
        }

        SellCapturedFish req = new SellCapturedFish(fishSpeciesName, captureTimeIso, price);

        progress.setVisibility(View.VISIBLE);

        api.sellCapturedFish(token, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "Sold! +" + price, Toast.LENGTH_SHORT).show();
                    updateBalance();
                    loadMyFishes(); // recargar lista
                } else {
                    Toast.makeText(ShopActivity.this, "Sell failed (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // --- HELPERS ---

    private String toIsoZulu(String raw) {
        if (raw == null) return null;

        raw = raw.trim();

        // Ya viene en ISO con Z (tu caso)
        if (raw.contains("T") && raw.endsWith("Z")) {
            // Si no tiene milisegundos, se los añadimos
            if (!raw.matches(".*\\.\\d{3}Z$")) {
                raw = raw.replace("Z", ".000Z");
            }
            return raw;
        }

        return null;
    }



}