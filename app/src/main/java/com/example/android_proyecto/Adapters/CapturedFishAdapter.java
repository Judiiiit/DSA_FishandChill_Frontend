package com.example.android_proyecto.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Models.CapturedFish;
import com.example.android_proyecto.Models.Fish;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Utils.FishSellCalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CapturedFishAdapter extends RecyclerView.Adapter<CapturedFishAdapter.FishViewHolder> {

    public interface OnFishClickListener {
        void onSellClick(CapturedFish capturedFish, int coinsGained);
    }

    private List<List<CapturedFish>> fishStacks = new ArrayList<>();
    private Set<String> soldFishKeys = new HashSet<>();
    private final OnFishClickListener listener;

    public CapturedFishAdapter(OnFishClickListener listener) {
        this.listener = listener;
    }

    public void setFishes(List<CapturedFish> rawFishes) {
        if (rawFishes == null) rawFishes = new ArrayList<>();

        Map<String, List<CapturedFish>> groups = new HashMap<>();
        for (CapturedFish cf : rawFishes) {
            String name = (cf.getFishSpecies() != null) ? cf.getFishSpecies().getSpeciesName() : "Unknown";
            if (!groups.containsKey(name)) {
                groups.put(name, new ArrayList<>());
            }
            groups.get(name).add(cf);
        }

        this.fishStacks = new ArrayList<>(groups.values());
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
        List<CapturedFish> stack = fishStacks.get(position);
        if (stack.isEmpty()) return;

        CapturedFish topFish = stack.get(0);
        Fish species = topFish.getFishSpecies();

        // 1. Stack Visuals
        int stackSize = stack.size();
        holder.tvStackCount.setText(String.valueOf(stackSize));
        holder.tvStackCount.setVisibility(stackSize > 1 ? View.VISIBLE : View.GONE);
        holder.cardBg1.setVisibility(stackSize > 1 ? View.VISIBLE : View.INVISIBLE);
        holder.cardBg2.setVisibility(stackSize > 2 ? View.VISIBLE : View.INVISIBLE);

        holder.btnRotate.setVisibility(stackSize > 1 ? View.VISIBLE : View.GONE);
        holder.btnRotate.setOnClickListener(v -> rotateStack(holder, stack, position));

        // 2. Basic Data
        String name = (species != null) ? species.getSpeciesName() : "Unknown";
        int rarity = (species != null) ? species.getRarity() : 1;
        double weight = topFish.getWeight();
        int coins = FishSellCalculator.calculateCoins(weight, rarity);

        holder.tvFishName.setText(name);
        holder.tvFishValue.setText("💰 " + coins);

        // --- UPDATED TEXT FORMATTING ---
        // Format: Weight 2.5kg\nCaptured: 17/03/25 (17:24:21)
        String weightStr = String.format(Locale.US, "Weight: %.1fkg", weight);
        String dateStr = "Captured: " + formatDate(topFish.getCaptureTime());

        holder.tvFishDesc.setText(weightStr + "\n" + dateStr);
        // -------------------------------

        if (species != null && species.getUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(RetrofitClient.SERVER_URL + species.getUrl())
                    .into(holder.imgFish);
        }

        // 3. Sell Logic
        String key = buildKey(name, weight, topFish.getCaptureTime());
        if (soldFishKeys.contains(key)) {
            holder.btnSell.setText("Sold");
            holder.btnSell.setEnabled(false);
            holder.btnSell.setAlpha(0.5f);
        } else {
            holder.btnSell.setText("Sell");
            holder.btnSell.setEnabled(true);
            holder.btnSell.setAlpha(1.0f);
            holder.btnSell.setOnClickListener(v -> {
                listener.onSellClick(topFish, coins);
            });
        }

        // 4. Colors & Gradients
        applyRarityVisuals(holder, rarity);
    }

    private void rotateStack(FishViewHolder holder, List<CapturedFish> stack, int adapterPosition) {
        if (stack.size() <= 1) return;

        holder.btnRotate.setEnabled(false);

        holder.cardFront.animate()
                .translationX(400f)
                .rotation(20f)
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        CapturedFish movedFish = stack.remove(0);
                        stack.add(movedFish);

                        holder.cardFront.setTranslationX(0f);
                        holder.cardFront.setRotation(0f);
                        holder.cardFront.setAlpha(1f);

                        notifyItemChanged(adapterPosition);
                        holder.btnRotate.setEnabled(true);
                    }
                }).start();
    }

    private void applyRarityVisuals(FishViewHolder holder, int rarity) {
        if (holder.rainbowAnimator != null) {
            holder.rainbowAnimator.cancel();
            holder.rainbowAnimator = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            holder.containerRight.setRenderEffect(RenderEffect.createBlurEffect(2.0f, 2.0f, Shader.TileMode.CLAMP));
        }

        if (rarity >= 3) {
            applyRainbowAnimation(holder);
        } else {
            int baseColor;
            if (rarity == 2) {
                baseColor = Color.parseColor("#a855f7"); // Purple
            } else {
                baseColor = Color.parseColor("#3bd671"); // Green
            }

            holder.tvFishName.setTextColor(baseColor);
            holder.tvFishName.setShadowLayer(15, 0, 0, baseColor);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            // --- UPDATED RADIUS: Reduced to 200f to fade out at borders ---
            imgBg.setGradientRadius(210f);
            // -----------------------------------------------
            int c1 = setAlpha(baseColor, 255);
            int c2 = setAlpha(baseColor, 230);
            int c3 = Color.TRANSPARENT;
            imgBg.setColors(new int[]{c1, c2, c3});
            holder.imgFish.setBackground(imgBg);

            GradientDrawable infoBg = new GradientDrawable();
            infoBg.setColor(setAlpha(baseColor, 30));
            infoBg.setCornerRadius(12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            infoBg.setStroke(2, setAlpha(baseColor, 80));
            holder.containerRight.setBackground(infoBg);
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
            // --- UPDATED RADIUS: Reduced to 200f to fade out at borders ---
            imgBg.setGradientRadius(210f);
            // ----------------------
            imgBg.setColors(new int[]{setAlpha(animatedColor, 150), setAlpha(animatedColor, 100), Color.TRANSPARENT});
            holder.imgFish.setBackground(imgBg);

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

    private String formatDate(String t) {
        if (t == null) return "-";
        try {
            // Input: yyyy-MM-dd'T'HH:mm:ss
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = inputFormat.parse(t);

            // Output: 17/03/25 (17:24:21)
            // dd/MM/yy (HH:mm:ss)
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy (HH:mm:ss)", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return t;
        }
    }

    private String buildKey(String name, double weight, String captureTime) {
        if (captureTime == null) captureTime = "-";
        return name + "|" + weight + "|" + captureTime;
    }

    @Override
    public int getItemCount() {
        return fishStacks.size();
    }

    static class FishViewHolder extends RecyclerView.ViewHolder {
        CardView cardFront, cardBg1, cardBg2;
        TextView tvFishName, tvFishDesc, tvFishValue, tvStackCount;
        Button btnSell;
        ImageButton btnRotate;
        ImageView imgFish;
        View containerRight;
        ValueAnimator rainbowAnimator;

        FishViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFront = itemView.findViewById(R.id.cardFront);
            cardBg1 = itemView.findViewById(R.id.cardBg1);
            cardBg2 = itemView.findViewById(R.id.cardBg2);
            tvStackCount = itemView.findViewById(R.id.tvStackCount);

            tvFishName = itemView.findViewById(R.id.tvFishName);
            tvFishDesc = itemView.findViewById(R.id.tvFishDesc);
            tvFishValue = itemView.findViewById(R.id.tvFishValue);
            btnSell = itemView.findViewById(R.id.btnSellFish);
            btnRotate = itemView.findViewById(R.id.btnRotate);
            imgFish = itemView.findViewById(R.id.imgFish);
            containerRight = itemView.findViewById(R.id.containerRight);
        }
    }
}