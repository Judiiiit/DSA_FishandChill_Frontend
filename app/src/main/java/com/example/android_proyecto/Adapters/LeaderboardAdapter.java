package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.LeaderboardEntry;
import com.example.android_proyecto.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

    private final List<LeaderboardEntry> items = new ArrayList<>();

    public void setItems(List<LeaderboardEntry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LeaderboardEntry e = items.get(position);

        int rank = position + 4; // porque top 1-3 se muestran arriba

        holder.tvRank.setText("#" + rank);
        holder.tvUsername.setText(e.getUsername());
        holder.tvTotal.setText(String.valueOf(e.getTotalFishes()));

        String avatarUrl = e.getAvatarUrl();
        Glide.with(holder.itemView.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.avatar_1)
                .error(R.drawable.avatar_1)
                .circleCrop()
                .into(holder.imgAvatar);


        // Desde #4 todo normal
        holder.containerRow.setBackgroundResource(R.drawable.bg_leaderboard_normal);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvTotal;
        ImageView imgAvatar;

        LinearLayout containerRow;

        VH(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            containerRow = itemView.findViewById(R.id.containerRow);
        }
    }
}
