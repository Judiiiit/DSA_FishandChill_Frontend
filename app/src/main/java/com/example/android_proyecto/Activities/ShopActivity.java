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

import com.example.android_proyecto.MainActivity;
import com.example.android_proyecto.Models.User;

import com.example.android_proyecto.Adapters.RodsAdapter;
import com.example.android_proyecto.Models.FishingRod;
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

    private RodsAdapter adapter;
    private ApiService api;
    private SessionManager session;
    private Button btnBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        tvCoins  = findViewById(R.id.tvCoins);
        rvRods   = findViewById(R.id.rvRods);
        progress = findViewById(R.id.progressShop);
        btnBack = findViewById(R.id.btnBack);

        api = RetrofitClient.getApiService();
        session = new SessionManager(this);

        adapter = new RodsAdapter(new ArrayList<>(), this::onBuyRodClicked);
        rvRods.setLayoutManager(new LinearLayoutManager(this));
        rvRods.setAdapter(adapter);

        loadBalance();
        loadRods();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadBalance() {
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            tvCoins.setText("Coins: (no tokens available)");
            Toast.makeText(this,
                    "No token saved. Please log in again.",
                    Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ShopActivity.this,
                            "Balance error. HTTP code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvCoins.setText("Coins: ?");
                Toast.makeText(ShopActivity.this,
                        "Connection error while retrieving balance: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void loadRods() {
        progress.setVisibility(View.VISIBLE);

        // 1) primero pedimos al backend que cargue el diccionario de cañas
        api.loadRodsDictionary().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Nos da igual el texto, lo importante es que lo intente.
                // Aunque devuelva 409 porque ya estaba cargado, seguimos.
                fetchRods();   // 2) ahora sí, pedimos la lista de cañas
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this,
                        "Error loading rod catalog: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchRods() {
        api.getRods().enqueue(new Callback<List<FishingRod>>() {
            @Override
            public void onResponse(Call<List<FishingRod>> call, Response<List<FishingRod>> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<FishingRod> rods = response.body();
                    adapter.setRods(rods);  // ya llama a notifyDataSetChanged()

                    if (rods.isEmpty()) {
                        Toast.makeText(ShopActivity.this,
                                "API returned 0 rods (empty list).",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ShopActivity.this,
                            "Error loading rods. HTTP code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<FishingRod>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this,
                        "Connection error while loading rods: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onBuyRodClicked(FishingRod rod) {
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this,
                    "No token saved. Purchase not possible.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        api.buyRod(token, rod.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(ShopActivity.this,
                            "You have purchased " + rod.getName(),
                            Toast.LENGTH_SHORT).show();
                    loadBalance();   // actualiza las coins
                    return;
                }

                // Aquí distinguimos los distintos tipos de error
                String mensaje = "Purchase error. HTTP code: " + response.code();

                try {
                    ResponseBody errorBody = response.errorBody();
                    String errorText = errorBody != null ? errorBody.string() : "";

                    if (errorText.contains("Not enough coins")) {
                        mensaje = "You do not have enough coins to buy this item.";
                    } else if (errorText.contains("Already owned")) {
                        mensaje = "Item already purchased.";
                    } else if (errorText.contains("Rod not found")) {
                        mensaje = "Item no longer available in the shop.";
                    }
                } catch (Exception e) {
                    // Si falla al leer el body, dejamos el mensaje genérico
                }

                Toast.makeText(ShopActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ShopActivity.this,
                        "Connection error while purchasing: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


}
