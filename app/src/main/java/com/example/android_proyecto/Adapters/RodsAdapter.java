package com.example.android_proyecto.Adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    // Helper interface to distinguish actions
    public interface OnRodActionListener {
        void onBuyClick(FishingRod rod);
        void onEquipClick(FishingRod rod);
    }

    private List<FishingRod> rods = new ArrayList<>();
    private Set<String> ownedRodNames = new HashSet<>();
    private String equippedRodName = ""; // Stores the name of the currently equipped rod
    private OnRodActionListener listener;

    public RodsAdapter(OnRodActionListener listener) {
        this.listener = listener;
    }

    public void setRods(List<FishingRod> rods) {
        this.rods = rods != null ? rods : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOwnedRodNames(Set<String> ownedRodNames) {
        this.ownedRodNames = ownedRodNames != null ? ownedRodNames : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setEquippedRodName(String name) {
        this.equippedRodName = name != null ? name : "";
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

        // 1. Basic Info
        holder.tvName.setText(rod.getName());
        String stats = "Power: " + rod.getPower() + "\nSpeed: " + rod.getSpeed() + "\nDurability: " + rod.getDurability();
        holder.tvStats.setText(stats);

        Glide.with(holder.itemView.getContext())
                .load(RetrofitClient.SERVER_URL + rod.getUrl())
                .into(holder.imgRod);

        // 2. Logic: Buy vs Equip vs Equipped
        boolean isOwned = ownedRodNames.contains(rod.getName());
        boolean isEquipped = rod.getName().equals(equippedRodName);

        if (isOwned) {
            holder.tvPrice.setVisibility(View.GONE); // Hide price if owned

            if (isEquipped) {
                // STATE: EQUIPPED
                holder.btnAction.setText("Equipped");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundColor(Color.parseColor("#44FFFFFF")); // Transparent/Disabled look
            } else {
                // STATE: OWNED BUT NOT EQUIPPED
                holder.btnAction.setText("Equip");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundColor(Color.parseColor("#2a9d6f")); // Greenish equip color
                holder.btnAction.setOnClickListener(v -> listener.onEquipClick(rod));
            }
        } else {
            // STATE: NOT OWNED (BUY)
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText("💰 " + rod.getPrice());

            holder.btnAction.setText("Buy");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setBackgroundColor(Color.parseColor("#2a86c7")); // Blue buy color
            holder.btnAction.setOnClickListener(v -> listener.onBuyClick(rod));
        }

        // 3. Rarity Styling (Colors from HTML)
        applyRarityStyling(holder, rod.getRarity() + "");
    }

    private void applyRarityStyling(RodViewHolder holder, String rarityStr) {
        // Parse rarity, default to 1 if not a number
        int rarity = 1;
        try {
            rarity = Integer.parseInt(rarityStr);
        } catch (NumberFormatException e) {
            rarity = 1;
        }

        String glowColor;
        String bgColor; // Background for the right info panel

        switch (rarity) {
            case 2: // Rare
                glowColor = "#3b82f6"; // Blue
                bgColor = "#203b82f6";
                break;
            case 3: // Epic
                glowColor = "#a855f7"; // Purple
                bgColor = "#20a855f7";
                break;
            case 4: // Legendary
                glowColor = "#ef4444"; // Red
                bgColor = "#30ef4444";
                break;
            case 5: // Mythic/Special
                glowColor = "#FFFFFF"; // White
                bgColor = "#20FFFFFF";
                break;
            default: // Common (1)
                glowColor = "#3bd671"; // Green
                bgColor = "#203bd671";
                break;
        }

        holder.tvName.setTextColor(Color.parseColor(glowColor));
        holder.tvName.setShadowLayer(12, 0, 0, Color.parseColor(glowColor));

        // Apply background tint to the right container
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(12);
        bg.setStroke(2, Color.parseColor(glowColor)); // Add border based on rarity
        holder.containerRight.setBackground(bg);
    }

    @Override
    public int getItemCount() {
        return rods.size();
    }

    static class RodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStats, tvPrice;
        Button btnAction;
        ImageView imgRod;
        View containerRight; // Reference to the right side for styling

        RodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRodName);
            tvStats = itemView.findViewById(R.id.tvStats);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAction = itemView.findViewById(R.id.btnAction);
            imgRod = itemView.findViewById(R.id.imgRod);
            containerRight = itemView.findViewById(R.id.containerRight);
        }
    }
}