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
        Fish fish = (cf != null) ? cf.getFishSpecies() : null;

        String name = (fish != null && fish.getSpeciesName() != null) ? fish.getSpeciesName() : "Unknown";
        int rarity = (fish != null) ? fish.getRarity() : 1;
        double weight = (cf != null) ? cf.getWeight() : 0.0;
        String captureTime = (cf != null) ? cf.getCaptureTime() : null;

        int coins = FishSellCalculator.calculateCoins(weight, rarity);

        holder.tvFishName.setText(name);
        holder.tvFishValue.setText("Value: " + coins);

        holder.tvFishDesc.setText(
                "Weight: " + String.format("%.2f", weight) + " kg\n" +
                        "Rarity: " + rarityToLabel(rarity) + "\n" +
                        "Caught: " + formatDate(captureTime)
        );

        if (fish != null && fish.getUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(RetrofitClient.SERVER_URL + fish.getUrl())
                    .into(holder.imgFish);
        } else {
            holder.imgFish.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        applyRarityVisuals(holder, String.valueOf(rarity));

        String key = buildKey(name, weight, captureTime);
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

    private void applyRarityVisuals(FishViewHolder holder, String rarityStr) {
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

        if (holder.containerInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            holder.containerInfo.setRenderEffect(RenderEffect.createBlurEffect(2.0f, 2.0f, Shader.TileMode.CLAMP));
        }

        if (rarity >= 5) {
            applyRainbowAnimation(holder);
        } else {
            int baseColor;
            switch (rarity) {
                case 2: baseColor = Color.parseColor("#3b82f6"); break;
                case 3: baseColor = Color.parseColor("#a855f7"); break;
                case 4: baseColor = Color.parseColor("#ff7f7f"); break;
                default: baseColor = Color.parseColor("#3bd671"); break;
            }

            holder.tvFishName.setTextColor(baseColor);
            holder.tvFishName.setShadowLayer(15, 0, 0, baseColor);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            imgBg.setGradientRadius(200f);
            int c1 = setAlpha(baseColor, 150);
            int c2 = setAlpha(baseColor, 60);
            int c3 = Color.TRANSPARENT;
            imgBg.setColors(new int[]{c1, c2, c3});
            holder.imgFish.setBackground(imgBg);

            if (holder.containerInfo != null) {
                GradientDrawable infoBg = new GradientDrawable();
                infoBg.setColor(setAlpha(baseColor, 30));
                infoBg.setCornerRadius(12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                infoBg.setStroke(2, setAlpha(baseColor, 80));
                holder.containerInfo.setBackground(infoBg);
            }
        }
    }

    private void applyRainbowAnimation(FishViewHolder holder) {
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

            holder.tvFishName.setTextColor(animatedColor);
            holder.tvFishName.setShadowLayer(20, 0, 0, Color.WHITE);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            imgBg.setGradientRadius(200f);
            int c1 = setAlpha(animatedColor, 150);
            int c2 = setAlpha(animatedColor, 60);
            int c3 = Color.TRANSPARENT;
            imgBg.setColors(new int[]{c1, c2, c3});
            holder.imgFish.setBackground(imgBg);

            if (holder.containerInfo != null) {
                GradientDrawable infoBg = new GradientDrawable();
                infoBg.setColor(setAlpha(animatedColor, 20));
                infoBg.setCornerRadius(12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                infoBg.setStroke(3, setAlpha(animatedColor, 120));
                holder.containerInfo.setBackground(infoBg);
            }
        });

        animator.start();
        holder.rainbowAnimator = animator;
    }

    private int setAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public int getItemCount() {
        return fishes.size();
    }

    static class FishViewHolder extends RecyclerView.ViewHolder {
        TextView tvFishName, tvFishDesc, tvFishValue;
        ImageButton btnSell;
        ImageView imgFish;
        View containerInfo;
        ValueAnimator rainbowAnimator;

        FishViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFishName = itemView.findViewById(R.id.tvFishName);
            tvFishDesc = itemView.findViewById(R.id.tvFishDesc);
            tvFishValue = itemView.findViewById(R.id.tvFishValue);
            btnSell = itemView.findViewById(R.id.btnSellFish);
            imgFish = itemView.findViewById(R.id.imgFish);
            containerInfo = itemView.findViewById(R.id.containerInfo);
        }
    }

    private String rarityToLabel(int rarity) {
        switch (rarity) {
            case 2: return "Rare";
            case 3: return "Epic";
            case 4: return "Mythic";
            case 5: return "Titanium";
            default: return "Common";
        }
    }

    private String formatDate(String t) {
        if (t == null) return "-";
        return t.replace("T", " ").replace("Z", "");
    }

    private String buildKey(String name, double weight, String captureTime) {
        if (captureTime == null) captureTime = "-";
        return name + "|" + weight + "|" + captureTime;
    }
}
