package com.example.android_proyecto.Adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.AvatarItem;
import com.example.android_proyecto.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Set;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.VH> {

    public interface OnAvatarClick {
        void onClick(AvatarItem item, boolean unlocked);
    }

    private final List<AvatarItem> items;
    private final Set<String> unlockedAchievements;
    private int selectedResId;
    private final OnAvatarClick listener;

    public AvatarAdapter(List<AvatarItem> items, Set<String> unlockedAchievements, int selectedResId, OnAvatarClick listener) {
        this.items = items;
        this.unlockedAchievements = unlockedAchievements;
        this.selectedResId = selectedResId;
        this.listener = listener;
    }

    public void setSelectedResId(int resId) {
        this.selectedResId = resId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AvatarItem item = items.get(position);
        int resId = item.getResId();

        holder.ivAvatar.setImageResource(resId);

        boolean selected = (resId == selectedResId);
        holder.ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);

        int strokeColor = selected
                ? ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light)
                : ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent);

        holder.cardAvatar.setStrokeColor(strokeColor);

        String req = item.getRequiredAchievementId();

        final boolean isUnlocked = (req == null || req.trim().isEmpty())
                || (unlockedAchievements != null && unlockedAchievements.contains(req));

        holder.lockedOverlay.setVisibility(isUnlocked ? View.GONE : View.VISIBLE);

        if (!isUnlocked) {
            String reason = item.getLockedText();
            holder.tvLockedReason.setText(reason != null && !reason.isEmpty() ? reason : "Locked");
            holder.ivAvatar.setAlpha(0.45f);

            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            holder.ivAvatar.setColorFilter(new ColorMatrixColorFilter(cm));
        } else {
            holder.ivAvatar.setAlpha(1f);
            holder.ivAvatar.clearColorFilter();
        }

        holder.itemView.setOnClickListener(v -> {
            if (isUnlocked) {
                v.setScaleX(1f);
                v.setScaleY(1f);

                ObjectAnimator sx1 = ObjectAnimator.ofFloat(v, View.SCALE_X, 1f, 0.94f);
                ObjectAnimator sy1 = ObjectAnimator.ofFloat(v, View.SCALE_Y, 1f, 0.94f);
                sx1.setDuration(70);
                sy1.setDuration(70);

                ObjectAnimator sx2 = ObjectAnimator.ofFloat(v, View.SCALE_X, 0.94f, 1.06f);
                ObjectAnimator sy2 = ObjectAnimator.ofFloat(v, View.SCALE_Y, 0.94f, 1.06f);
                sx2.setDuration(90);
                sy2.setDuration(90);

                ObjectAnimator sx3 = ObjectAnimator.ofFloat(v, View.SCALE_X, 1.06f, 1f);
                ObjectAnimator sy3 = ObjectAnimator.ofFloat(v, View.SCALE_Y, 1.06f, 1f);
                sx3.setDuration(90);
                sy3.setDuration(90);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(sx1, sy1);
                AnimatorSet set2 = new AnimatorSet();
                set2.playTogether(sx2, sy2);
                AnimatorSet set3 = new AnimatorSet();
                set3.playTogether(sx3, sy3);

                AnimatorSet all = new AnimatorSet();
                all.playSequentially(set, set2, set3);
                all.start();
            }

            if (listener != null) listener.onClick(item, isUnlocked);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView cardAvatar;
        final ImageView ivAvatar;
        final ImageView ivSelected;
        final LinearLayout lockedOverlay;
        final TextView tvLockedReason;

        VH(@NonNull View itemView) {
            super(itemView);
            cardAvatar = itemView.findViewById(R.id.cardAvatar);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            lockedOverlay = itemView.findViewById(R.id.lockedOverlay);
            tvLockedReason = itemView.findViewById(R.id.tvLockedReason);
        }
    }
}
