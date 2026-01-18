package com.example.android_proyecto.Adapters;

import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Models.TeamMember;
import com.example.android_proyecto.R;
import com.example.android_proyecto.glide.SvgSoftwareLayerSetter;

import java.util.List;

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.VH> {

    private final List<TeamMember> items;

    public TeamMembersAdapter(List<TeamMember> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvMemberName;
        TextView tvMemberPoints;

        VH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberPoints = itemView.findViewById(R.id.tvMemberPoints);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TeamMember m = items.get(position);

        holder.tvMemberName.setText(m != null && m.getName() != null ? m.getName() : "-");
        holder.tvMemberPoints.setText(m != null ? String.valueOf(m.getPoints()) : "0");

        String url = (m != null) ? m.getAvatar() : null;

        Glide.with(holder.itemView.getContext()).clear(holder.imgAvatar);
        holder.imgAvatar.setImageDrawable(null);

        if (url == null || url.trim().isEmpty()) {
            holder.imgAvatar.setImageResource(R.drawable.avatar_1);
            return;
        }

        String u = url.trim().toLowerCase();
        if (u.contains("/svg") || u.contains("svg?") || u.endsWith(".svg")) {
            Glide.with(holder.itemView.getContext())
                    .as(PictureDrawable.class)
                    .load(url)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .listener(new SvgSoftwareLayerSetter())
                    .into(holder.imgAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .centerCrop()
                    .into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}
