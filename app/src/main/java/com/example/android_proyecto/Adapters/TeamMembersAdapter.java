package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.TeamMember;
import com.example.android_proyecto.R;

import java.util.List;

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.VH> {

    private final List<TeamMember> items;

    public TeamMembersAdapter(List<TeamMember> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        TextView tvMemberPoints;

        VH(@NonNull View itemView) {
            super(itemView);
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
        if (holder.tvMemberPoints != null) {
            holder.tvMemberPoints.setText(m != null ? String.valueOf(m.getPoints()) : "0");
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}
