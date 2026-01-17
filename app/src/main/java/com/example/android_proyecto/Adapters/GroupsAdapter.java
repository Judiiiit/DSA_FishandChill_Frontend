package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.Group;
import com.example.android_proyecto.R;

import java.util.List;
import java.util.Set;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.VH> {

    public interface OnGroupActionListener {
        void onOpenMembers(Group group);
        void onJoin(Group group);
    }

    private final List<Group> groups;
    private final Set<Integer> joinedGroupIds;
    private final OnGroupActionListener listener;

    public GroupsAdapter(List<Group> groups, Set<Integer> joinedGroupIds, OnGroupActionListener listener) {
        this.groups = groups;
        this.joinedGroupIds = joinedGroupIds;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        Button btnJoinGroup;
        LinearLayout layoutGroup;

        VH(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            btnJoinGroup = itemView.findViewById(R.id.btnJoinGroup);
            layoutGroup = itemView.findViewById(R.id.layoutGroup);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Group g = groups.get(position);
        holder.tvGroupName.setText(g.getName());

        boolean joined = joinedGroupIds.contains(g.getId());

        if (joined) {
            holder.btnJoinGroup.setText("Joined");
            holder.btnJoinGroup.setEnabled(false);
            holder.layoutGroup.setAlpha(0.65f);
        } else {
            holder.btnJoinGroup.setText("Join");
            holder.btnJoinGroup.setEnabled(true);
            holder.layoutGroup.setAlpha(1.0f);
        }

        holder.btnJoinGroup.setOnClickListener(v -> {
            if (listener != null) listener.onJoin(g);
        });

        holder.layoutGroup.setOnClickListener(v -> {
            if (listener != null) listener.onOpenMembers(g);
        });
    }

    @Override
    public int getItemCount() {
        return groups != null ? groups.size() : 0;
    }
}
