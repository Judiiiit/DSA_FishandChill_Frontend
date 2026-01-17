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

import com.example.android_proyecto.Adapters.CapturedFishAdapter;
import com.example.android_proyecto.Adapters.RodsAdapter;
import com.example.android_proyecto.Models.CapturedFish;
import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.Models.SellCapturedFish;
import com.example.android_proyecto.Models.User;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.AchievementsManager;
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

    private TextView tvCoins, tvTotalFishes, tvInitialMessage, tvShopTitle;
    private RecyclerView rvRods, rvFishes;
    private ProgressBar progress;
    private Button btnBack, btnRods, btnFishes;

    private RodsAdapter rodsAdapter;
    private CapturedFishAdapter fishesAdapter;

    private ApiService api;
    private SessionManager session;
    private String token;

    private List<FishingRod> allRodsList = new ArrayList<>();
    private Set<String> ownedRodNames = new HashSet<>();
    private String currentEquippedRod = "";

    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_FISH_TOTAL_PREFIX = "fish_total_";
    private static final String KEY_FISH_SEEN_PREFIX  = "fish_seen_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        new AchievementsManager(this).unlock(AchievementsManager.A_OPEN_SHOP);

        tvCoins = findViewById(R.id.tvCoins);
        tvTotalFishes = findViewById(R.id.tvTotalFishes);
        tvInitialMessage = findViewById(R.id.tvInitialMessage);
        tvShopTitle = findViewById(R.id.tvShopTitle);

        rvRods = findViewById(R.id.rvRods);
        rvFishes = findViewById(R.id.rvFishes);
        progress = findViewById(R.id.progressShop);

        btnBack = findViewById(R.id.btnBack);
        btnRods = findViewById(R.id.btnRods);
        btnFishes = findViewById(R.id.btnFishes);

        api = RetrofitClient.getApiService();
        session = new SessionManager(this);
        token = session.getToken();

        renderTotalFishes();

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

        rvRods.setLayoutManager(new GridLayoutManager(this, 2));
        rvRods.setAdapter(rodsAdapter);

        fishesAdapter = new CapturedFishAdapter((capturedFish, coinsGained) -> sellFishToBackend(capturedFish, coinsGained));
        rvFishes.setLayoutManager(new GridLayoutManager(this, 2));
        rvFishes.setAdapter(fishesAdapter);

        tvShopTitle.setVisibility(View.VISIBLE);
        tvInitialMessage.setVisibility(View.VISIBLE);
        rvRods.setVisibility(View.GONE);
        rvFishes.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());
        btnRods.setOnClickListener(v -> showShopView());
        btnFishes.setOnClickListener(v -> showFishesView());

        loadAllShopData();
    }

    private void loadAllShopData() {
        if (token == null) return;
        progress.setVisibility(View.VISIBLE);

        api.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    tvCoins.setText("Coins: " + u.getCoins());
                    currentEquippedRod = u.getEquippedFishingRod();
                    rodsAdapter.setEquippedRodName(currentEquippedRod);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) { }
        });

        api.getRods().enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRodsList = response.body();
                    fetchOwnedRods();
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

    private void showShopView() {
        rvFishes.setVisibility(View.GONE);
        rvRods.setVisibility(View.VISIBLE);
        tvInitialMessage.setVisibility(View.GONE);
        tvShopTitle.setVisibility(View.GONE);

        rodsAdapter.setOwnedRodNames(ownedRodNames);
        rodsAdapter.setEquippedRodName(currentEquippedRod);
        rodsAdapter.setRods(allRodsList);
    }

    private void showFishesView() {
        rvRods.setVisibility(View.GONE);
        tvInitialMessage.setVisibility(View.GONE);
        tvShopTitle.setVisibility(View.GONE);
        rvFishes.setVisibility(View.VISIBLE);
        loadMyFishes();
    }

    private void loadMyFishes() {
        progress.setVisibility(View.VISIBLE);

        api.getMyCapturedFishes(token).enqueue(new Callback<List<CapturedFish>>() {
            @Override
            public void onResponse(Call<List<CapturedFish>> call, Response<List<CapturedFish>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<CapturedFish> fishes = response.body();
                    updateTotalFishesIfNew(fishes);
                    fishesAdapter.setFishes(fishes);
                }
            }

            @Override
            public void onFailure(Call<List<CapturedFish>> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void buyRod(FishingRod rod) {
        progress.setVisibility(View.VISIBLE);
        api.buyRod(token, rod.getName()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "Bought: " + rod.getName(), Toast.LENGTH_SHORT).show();
                    ownedRodNames.add(rod.getName());
                    rodsAdapter.setOwnedRodNames(ownedRodNames);
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

    private void sellFishToBackend(CapturedFish cf, int price) {
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cf == null || cf.getFishSpecies() == null || cf.getFishSpecies().getSpeciesName() == null) {
            Toast.makeText(this, "Fish data missing", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cf.getCaptureTime() == null) {
            Toast.makeText(this, "Capture time missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String fishSpeciesName = cf.getFishSpecies().getSpeciesName();
        String captureTimeIso = toIsoZulu(cf.getCaptureTime());
        if (captureTimeIso == null) {
            Toast.makeText(this, "Invalid captureTime", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        SellCapturedFish req = new SellCapturedFish(fishSpeciesName, captureTimeIso, price);

        api.sellCapturedFish(token, req).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call,
                                   retrofit2.Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this, "Sold! +" + price + " coins", Toast.LENGTH_SHORT).show();
                    refreshUserData();
                    loadMyFishes();
                } else {
                    Toast.makeText(ShopActivity.this, "Sell failed (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this, "Network error selling fish: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String toIsoZulu(String raw) {
        if (raw == null) return null;
        raw = raw.trim();

        if (raw.contains("T") && raw.endsWith("Z")) {
            if (!raw.matches(".*\\.\\d{3}Z$")) {
                raw = raw.replace("Z", ".000Z");
            }
            return raw;
        }

        java.text.SimpleDateFormat[] inputs = new java.text.SimpleDateFormat[] {
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", java.util.Locale.US),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US),
        };

        java.text.SimpleDateFormat output =
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
        output.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        for (java.text.SimpleDateFormat in : inputs) {
            in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            try {
                java.util.Date d = in.parse(raw);
                if (d != null) return output.format(d);
            } catch (java.text.ParseException ignored) {}
        }

        return null;
    }

    private void renderTotalFishes() {
        if (tvTotalFishes == null) return;

        if (session == null) {
            session = new SessionManager(this);
        }

        String username = session.getUsername();
        if (username == null) {
            tvTotalFishes.setText("Fishes caught: 0");
            return;
        }

        int total = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getInt(KEY_FISH_TOTAL_PREFIX + username, 0);

        tvTotalFishes.setText("Fishes caught: " + total);
    }

    private void updateTotalFishesIfNew(List<CapturedFish> fishes) {
        if (tvTotalFishes == null) return;

        if (session == null) {
            session = new SessionManager(this);
        }

        String username = session.getUsername();
        if (username == null || fishes == null) return;

        var sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        java.util.Set<String> seen = new java.util.HashSet<>(
                sp.getStringSet(KEY_FISH_SEEN_PREFIX + username, new java.util.HashSet<>())
        );

        int total = sp.getInt(KEY_FISH_TOTAL_PREFIX + username, 0);

        boolean changed = false;

        for (CapturedFish cf : fishes) {
            String key = buildFishKeyForTotal(cf);
            if (key != null && !seen.contains(key)) {
                seen.add(key);
                total++;
                changed = true;
            }
        }

        if (changed) {
            sp.edit()
                    .putStringSet(KEY_FISH_SEEN_PREFIX + username, seen)
                    .putInt(KEY_FISH_TOTAL_PREFIX + username, total)
                    .apply();
        }

        tvTotalFishes.setText("Total fishes caught: " + total);
    }

    private String buildFishKeyForTotal(CapturedFish cf) {
        if (cf == null) return null;
        if (cf.getFishSpecies() == null || cf.getFishSpecies().getSpeciesName() == null) return null;
        if (cf.getCaptureTime() == null) return null;
        return cf.getFishSpecies().getSpeciesName() + "|" + cf.getCaptureTime();
    }
}
