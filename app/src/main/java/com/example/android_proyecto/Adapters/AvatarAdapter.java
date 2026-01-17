package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.R;
import com.google.android.material.card.MaterialCardView;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.VH> {

    public interface OnAvatarClick {
        void onClick(int avatarResId);
    }

    private final int[] avatarResIds;
    private int selectedResId;
    private final OnAvatarClick listener;

    public AvatarAdapter(int[] avatarResIds, int selectedResId, OnAvatarClick listener) {
        this.avatarResIds = avatarResIds;
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
        int resId = avatarResIds[position];
        holder.ivAvatar.setImageResource(resId);

        boolean selected = (resId == selectedResId);
        int strokeColor = selected
                ? ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light)
                : ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent);

        holder.cardAvatar.setStrokeColor(strokeColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(resId);
        });
    }

    @Override
    public int getItemCount() {
        return avatarResIds.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView cardAvatar;
        final ImageView ivAvatar;

        VH(@NonNull View itemView) {
            super(itemView);
            cardAvatar = itemView.findViewById(R.id.cardAvatar);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
