package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.AchievementItem;
import com.example.android_proyecto.R;

import java.util.ArrayList;
import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.VH> {

    private final List<AchievementItem> items = new ArrayList<>();

    public void setItems(List<AchievementItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AchievementItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
        holder.imgAchievement.setImageResource(item.getIconRes());

        if (item.isUnlocked()) {
            holder.tvState.setText("UNLOCKED");
            holder.tvState.setTextColor(0xFF4CAF50);
            holder.imgState.setImageResource(R.drawable.ic_check);
            holder.container.setAlpha(1.0f);
        } else {
            holder.tvState.setText("LOCKED");
            holder.tvState.setTextColor(0xFFBDBDBD);
            holder.imgState.setImageResource(R.drawable.ic_lock);
            holder.container.setAlpha(0.6f);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        LinearLayout container;
        ImageView imgAchievement;
        ImageView imgState;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvState;

        VH(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.containerAchievement);
            imgAchievement = itemView.findViewById(R.id.imgAchievement);
            imgState = itemView.findViewById(R.id.imgState);
            tvTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvDesc = itemView.findViewById(R.id.tvAchievementDesc);
            tvState = itemView.findViewById(R.id.tvAchievementState);
        }
    }
}
