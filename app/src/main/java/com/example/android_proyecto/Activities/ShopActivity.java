package com.example.android_proyecto.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Changed to Linear for better card look, or stick to Grid
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
    private String currentEquippedRod = ""; // New: Track equipped rod

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
        btnFishes = findViewById(R.id.btnFishes);

        // Init Service
        api = RetrofitClient.getApiService();
        session = new SessionManager(this);
        token = session.getToken();

        // Init Adapter with BOTH listeners (Buy and Equip)
        rodsAdapter = new RodsAdapter(new RodsAdapter.OnRodActionListener() {
            @Override
            public void onBuyClick(FishingRod rod) {
                buyRod(rod);
            }

            @Override
            public void onEquipClick(FishingRod rod) {
                equipRod(rod);
            }
        });

        // Use GridLayout 1 column if cards are wide, or 2 if screen is large.
        // Given the new Card layout is wide, 1 column might look better on phones, but 2 works on tablet.
        rvRods.setLayoutManager(new GridLayoutManager(this, 2));
        rvRods.setAdapter(rodsAdapter);

        fishesAdapter = new CapturedFishAdapter((capturedFish, coinsGained) -> sellFishToBackend(capturedFish, coinsGained));
        rvFishes.setLayoutManager(new GridLayoutManager(this, 2));
        rvFishes.setAdapter(fishesAdapter);

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnRods.setOnClickListener(v -> showShopView());
        btnFishes.setOnClickListener(v -> showFishesView());

        // Initial Load
        loadAllShopData();
    }

    private void loadAllShopData() {
        if (token == null) return;
        progress.setVisibility(View.VISIBLE);

        // 1. Get User Profile (Coins + Equipped Rod)
        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    tvCoins.setText("Coins: " + u.getCoins());

                    // Assuming User model has this field. If not, add getEquippedFishingRod() to User.java
                    currentEquippedRod = u.getEquippedFishingRod();
                    rodsAdapter.setEquippedRodName(currentEquippedRod);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) { }
        });

        // 2. Get All Rods
        api.getRods().enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRodsList = response.body();
                    fetchOwnedRods(); // Chain call
                } else {
                    progress.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void fetchOwnedRods() {
        api.getMyOwnedFishingRods(token).enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ownedRodNames.clear();
                    for (FishingRod rod : response.body()) {
                        ownedRodNames.add(rod.getName());
                    }
                }
            }
            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    // --- VIEW SWITCHING ---

    private void showShopView() {
        rvFishes.setVisibility(View.GONE);
        rvRods.setVisibility(View.VISIBLE);
        tvInitialMessage.setVisibility(View.GONE);

        rodsAdapter.setOwnedRodNames(ownedRodNames);
        rodsAdapter.setEquippedRodName(currentEquippedRod);
        rodsAdapter.setRods(allRodsList); // Show ALL items
    }

    private void showInventoryView() {
        rvFishes.setVisibility(View.GONE);
        rvRods.setVisibility(View.VISIBLE);
        tvInitialMessage.setVisibility(View.GONE);

        List<FishingRod> myInventory = new ArrayList<>();
        for (FishingRod rod : allRodsList) {
            if (ownedRodNames.contains(rod.getName())) {
                myInventory.add(rod);
            }
        }
        rodsAdapter.setOwnedRodNames(ownedRodNames);
        rodsAdapter.setEquippedRodName(currentEquippedRod);
        rodsAdapter.setRods(myInventory); // Show ONLY owned
    }

    // ... (keep showFishesView, loadMyFishes as they were) ...
    private void showFishesView() {
        rvRods.setVisibility(View.GONE);
        tvInitialMessage.setVisibility(View.GONE);
        rvFishes.setVisibility(View.VISIBLE);
        loadMyFishes();
    }

    private void loadMyFishes() {
        // ... (Keep existing implementation) ...
        // Just for brevity in this response, keep your existing logic here
        progress.setVisibility(View.VISIBLE);
        api.getMyCapturedFishes(token).enqueue(new Callback<List<CapturedFish>>() {
            @Override
            public void onResponse(Call<List<CapturedFish>> call, Response<List<CapturedFish>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    fishesAdapter.setFishes(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<CapturedFish>> call, Throwable t) { progress.setVisibility(View.GONE); }
        });
    }

    // --- ACTIONS ---

    private void buyRod(FishingRod rod) {
        progress.setVisibility(View.VISIBLE);
        api.buyRod(token, rod.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "Bought: " + rod.getName(), Toast.LENGTH_SHORT).show();

                    // Update state
                    ownedRodNames.add(rod.getName());
                    rodsAdapter.setOwnedRodNames(ownedRodNames);

                    // Refresh Coins
                    refreshUserData();
                } else {
                    Toast.makeText(ShopActivity.this, "Failed to buy.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void equipRod(FishingRod rod) {
        progress.setVisibility(View.VISIBLE);
        api.equipRod(token, rod.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "Equipped: " + rod.getName(), Toast.LENGTH_SHORT).show();

                    // Update local state immediately for snappy UI
                    currentEquippedRod = rod.getName();
                    rodsAdapter.setEquippedRodName(currentEquippedRod);

                } else {
                    Toast.makeText(ShopActivity.this, "Failed to equip.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void refreshUserData() {
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

    // ... (Keep existing sellFishToBackend and helpers) ...
    private void sellFishToBackend(CapturedFish cf, int price) {
        // ... Keep existing logic
    }
}