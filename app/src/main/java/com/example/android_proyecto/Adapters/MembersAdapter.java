package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.GroupUser;
import com.example.android_proyecto.R;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.VH> {

    private final List<GroupUser> items;

    public MembersAdapter(List<GroupUser> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMemberName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
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
        GroupUser u = items.get(position);
        holder.tvMemberName.setText(u != null ? u.getUsername() : "-");
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}
