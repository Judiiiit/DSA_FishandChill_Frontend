package com.example.android_proyecto.Adapters;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
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

    public interface OnRodActionListener {
        void onBuyClick(FishingRod rod);
        void onEquipClick(FishingRod rod);
    }

    private List<FishingRod> rods = new ArrayList<>();
    private Set<String> ownedRodNames = new HashSet<>();
    private String equippedRodName = "";
    private final OnRodActionListener listener;

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

        // --- 1. Basic Info ---
        holder.tvName.setText(rod.getName());
        String stats = "Power: " + rod.getPower() +
                "\nSpeed: " + String.format("%.1f", rod.getSpeed()) +
                "\nDurab.: " + rod.getDurability();
        holder.tvStats.setText(stats);

        Glide.with(holder.itemView.getContext())
                .load(RetrofitClient.SERVER_URL + rod.getUrl())
                .into(holder.imgRod);

        // --- 2. Button Logic with Custom Styling ---
        boolean isOwned = ownedRodNames.contains(rod.getName());
        boolean isEquipped = rod.getName().equals(equippedRodName);

        if (isOwned) {
            holder.tvPrice.setVisibility(View.GONE);
            if (isEquipped) {
                holder.btnAction.setText("Equipped");
                holder.btnAction.setEnabled(false);
                // Style: Transparent Gray
                styleButton(holder.btnAction, Color.parseColor("#44FFFFFF"), Color.parseColor("#66FFFFFF"));
            } else {
                holder.btnAction.setText("Equip");
                holder.btnAction.setEnabled(true);
                // Style: Green #2a9d6f
                styleButton(holder.btnAction, Color.parseColor("#2a9d6f"), darkenColor(Color.parseColor("#2a9d6f")));
                holder.btnAction.setOnClickListener(v -> listener.onEquipClick(rod));
            }
        } else {
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText("💰 " + rod.getPrice());
            holder.btnAction.setText("Buy");
            holder.btnAction.setEnabled(true);
            // Style: Blue #2a86c7
            styleButton(holder.btnAction, Color.parseColor("#2a86c7"), darkenColor(Color.parseColor("#2a86c7")));
            holder.btnAction.setOnClickListener(v -> listener.onBuyClick(rod));
        }

        // --- 3. Rarity Visuals ---
        applyRarityVisuals(holder, rod.getRarity() + "");
    }

    private void styleButton(Button btn, int bgColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(bgColor);
        drawable.setCornerRadius(10 * btn.getContext().getResources().getDisplayMetrics().density);
        drawable.setStroke((int) (2 * btn.getContext().getResources().getDisplayMetrics().density), strokeColor);
        btn.setBackground(drawable);
    }

    private int darkenColor(int color) {
        float factor = 0.7f;
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    private void applyRarityVisuals(RodViewHolder holder, String rarityStr) {
        int rarity;
        try {
            rarity = Integer.parseInt(rarityStr);
        } catch (NumberFormatException e) {
            rarity = 1;
        }

        if (holder.rainbowAnimator != null) {
            holder.rainbowAnimator.cancel();
            holder.rainbowAnimator = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            holder.containerRight.setRenderEffect(RenderEffect.createBlurEffect(2.0f, 2.0f, Shader.TileMode.CLAMP));
        }

        if (rarity == 5) {
            applyRainbowAnimation(holder);
        } else {
            int baseColor;
            switch (rarity) {
                case 2: baseColor = Color.parseColor("#3b82f6"); break; // Blue
                case 3: baseColor = Color.parseColor("#a855f7"); break; // Purple
                // CHANGED: Lighter Red (#ff7f7f) instead of Dark Red (#ef4444)
                case 4: baseColor = Color.parseColor("#ff7f7f"); break;
                default: baseColor = Color.parseColor("#3bd671"); break; // Green (1)
            }

            holder.tvName.setTextColor(baseColor);
            holder.tvName.setShadowLayer(15, 0, 0, baseColor);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            imgBg.setGradientRadius(210f);
            int c1 = setAlpha(baseColor, 150);
            int c2 = setAlpha(baseColor, 100);
            int c3 = Color.TRANSPARENT;
            imgBg.setColors(new int[]{c1, c2, c3});
            holder.imgRod.setBackground(imgBg);

            GradientDrawable infoBg = new GradientDrawable();
            infoBg.setColor(setAlpha(baseColor, 30));
            infoBg.setCornerRadius(12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            infoBg.setStroke(2, setAlpha(baseColor, 80));
            holder.containerRight.setBackground(infoBg);
        }
    }

    private void applyRainbowAnimation(RodViewHolder holder) {
        int[] rainbowColors = {
                Color.parseColor("#ff6464"), Color.parseColor("#ffc864"),
                Color.parseColor("#c8ff64"), Color.parseColor("#64ffc8"),
                Color.parseColor("#64c8ff"), Color.parseColor("#c864ff"),
                Color.parseColor("#ff6464")
        };

        ValueAnimator animator = ValueAnimator.ofArgb(rainbowColors);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            int animatedColor = (int) animation.getAnimatedValue();

            holder.tvName.setTextColor(animatedColor);
            holder.tvName.setShadowLayer(20, 0, 0, Color.WHITE);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            imgBg.setGradientRadius(210f);
            int c1 = setAlpha(animatedColor, 150);
            int c2 = setAlpha(animatedColor, 100);
            int c3 = Color.TRANSPARENT;
            imgBg.setColors(new int[]{c1, c2, c3});
            holder.imgRod.setBackground(imgBg);

            GradientDrawable infoBg = new GradientDrawable();
            infoBg.setColor(setAlpha(animatedColor, 20));
            infoBg.setCornerRadius(12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            infoBg.setStroke(3, setAlpha(animatedColor, 120));
            holder.containerRight.setBackground(infoBg);
        });

        animator.start();
        holder.rainbowAnimator = animator;
    }

    private int setAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public int getItemCount() {
        return rods.size();
    }

    static class RodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStats, tvPrice;
        Button btnAction;
        ImageView imgRod;
        View containerRight;
        ValueAnimator rainbowAnimator;

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