package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Models.CapturedFish;
import com.example.android_proyecto.Models.Fish;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Utils.FishSellCalculator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CapturedFishAdapter extends RecyclerView.Adapter<CapturedFishAdapter.FishViewHolder> {

    public interface OnFishClickListener {
        void onSellClick(CapturedFish capturedFish, int coinsGained);
    }

    private List<CapturedFish> fishes = new ArrayList<>();
    private Set<String> soldFishKeys = new HashSet<>();
    private OnFishClickListener listener;

    public CapturedFishAdapter(OnFishClickListener listener) {
        this.listener = listener;
    }

    public void setFishes(List<CapturedFish> fishes) {
        this.fishes = fishes != null ? fishes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSoldFishKeys(Set<String> soldFishKeys) {
        this.soldFishKeys = soldFishKeys != null ? soldFishKeys : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_captured_fish, parent, false);
        return new FishViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FishViewHolder holder, int position) {
        CapturedFish cf = fishes.get(position);
        Fish fish = (cf != null) ? cf.getSpeciesFish() : null;

        String name = (fish != null && fish.getSpeciesName() != null) ? fish.getSpeciesName() : "Unknown";
        int rarity = (fish != null) ? fish.getRarity() : 1; // 1/2/3
        double weight = (cf != null) ? cf.getWeight() : 0.0;
        Timestamp ts = (cf != null) ? cf.getCaptureTime() : null;

        int coins = FishSellCalculator.calculateCoins(weight, rarity);

        holder.tvFishName.setText(name);
        holder.tvFishValue.setText("Value: " + coins);

        holder.tvFishDesc.setText(
                "Weight: " + String.format("%.2f", weight) + " kg\n" + "Rarity: " + rarityToLabel(rarity) + "\n" + "Caught: " + formatDate(ts)
        );

        // Image (igual que rods)
        if (fish != null && fish.getUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(RetrofitClient.SERVER_URL + fish.getUrl())
                    .into(holder.imgFish);
        } else {
            holder.imgFish.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Sell button logic (mismo estilo que rods)
        String key = buildKey(name, weight, ts);
        boolean isSold = soldFishKeys.contains(key);

        if (isSold) {
            holder.btnSell.setEnabled(false);
            holder.btnSell.setAlpha(0.4f);
            holder.btnSell.setImageResource(android.R.drawable.checkbox_on_background);
            holder.btnSell.setOnClickListener(null);
        } else {
            holder.btnSell.setEnabled(true);
            holder.btnSell.setAlpha(1.0f);
            holder.btnSell.setImageResource(android.R.drawable.ic_menu_add);
            holder.btnSell.setOnClickListener(v -> {
                if (listener != null) listener.onSellClick(cf, coins);
            });
        }
    }

    @Override
    public int getItemCount() {
        return fishes.size();
    }

    static class FishViewHolder extends RecyclerView.ViewHolder {
        TextView tvFishName, tvFishDesc, tvFishValue;
        ImageButton btnSell;
        ImageView imgFish;

        FishViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFishName = itemView.findViewById(R.id.tvFishName);
            tvFishDesc = itemView.findViewById(R.id.tvFishDesc);
            tvFishValue = itemView.findViewById(R.id.tvFishValue);
            btnSell = itemView.findViewById(R.id.btnSellFish);
            imgFish = itemView.findViewById(R.id.imgFish);
        }
    }

    // ---------- Helpers ----------

    private String rarityToLabel(int rarity) {
        switch (rarity) {
            case 2: return "Rare";
            case 3: return "Legendary";
            default: return "Common";
        }
    }

    /** Timestamp -> "YYYY-MM-DD" */
    private String formatDate(Timestamp ts) {
        if (ts == null) return "-";
        String s = ts.toString(); // "2025-12-24 12:34:56.0"
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private String buildKey(String name, double weight, Timestamp ts) {
        String time = (ts != null) ? ts.toString() : "-";
        return name + "|" + String.format("%.4f", weight) + "|" + time;
    }
}
