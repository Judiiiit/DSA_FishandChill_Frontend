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
import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RodsAdapter extends RecyclerView.Adapter<RodsAdapter.RodViewHolder> {

    public interface OnRodClickListener {
        void onBuyClick(FishingRod rod);
    }

    private List<FishingRod> rods = new ArrayList<>();
    private Set<String> ownedRodNames = new HashSet<>();
    private OnRodClickListener listener;
    private boolean isInventoryMode = false;

    public RodsAdapter(OnRodClickListener listener) {
        this.listener = listener;
    }

    // Update the list of rods to display
    public void setRods(List<FishingRod> rods) {
        this.rods = rods != null ? rods : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Update the list of names the user owns
    public void setOwnedRodNames(Set<String> ownedRodNames) {
        this.ownedRodNames = ownedRodNames != null ? ownedRodNames : new HashSet<>();
        notifyDataSetChanged();
    }

    // Toggle between Shop view (buy buttons) and Inventory view (no buttons)
    public void setInventoryMode(boolean isInventoryMode) {
        this.isInventoryMode = isInventoryMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rod, parent, false);
        return new RodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RodViewHolder holder, int position) {
        FishingRod rod = rods.get(position);

        holder.tvName.setText(rod.getName());
        holder.tvPrice.setText("Price: " + rod.getPrice());

        String desc = "Speed: " + String.format("%.1f", rod.getSpeed()) + "\nPower: " + String.format("%.1f", rod.getPower()) + "\nRarity: " + rod.getRarity();
        holder.tvDesc.setText(desc);

        Glide.with(holder.itemView.getContext())
                .load(RetrofitClient.SERVER_URL + rod.getUrl())
                .into(holder.imgRod);

        // LOGIC: Handle Buy Button State
        if (isInventoryMode) {
            // Inventory Mode: Hide buy button completely
            holder.btnBuy.setVisibility(View.GONE);
        } else {
            // Shop Mode: Show button
            holder.btnBuy.setVisibility(View.VISIBLE);

            boolean isOwned = ownedRodNames.contains(rod.getName());

            if (isOwned) {
                // Owned: Disable button and gray it out
                holder.btnBuy.setEnabled(false);
                holder.btnBuy.setAlpha(0.4f);
                holder.btnBuy.setImageResource(android.R.drawable.checkbox_on_background); // Optional: change icon to checkmark
                holder.btnBuy.setOnClickListener(null);
            } else {
                // Not Owned: Enable button
                holder.btnBuy.setEnabled(true);
                holder.btnBuy.setAlpha(1.0f);
                holder.btnBuy.setImageResource(android.R.drawable.ic_menu_add); // Make sure you have a cart icon or similar
                holder.btnBuy.setOnClickListener(v -> {
                    if (listener != null) listener.onBuyClick(rod);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return rods.size();
    }

    static class RodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;
        ImageButton btnBuy;
        ImageView imgRod;

        RodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRodName);
            tvDesc = itemView.findViewById(R.id.tvRodDesc);
            tvPrice = itemView.findViewById(R.id.tvRodPrice);
            btnBuy = itemView.findViewById(R.id.btnBuy);
            imgRod = itemView.findViewById(R.id.imgRod);
        }
    }
}