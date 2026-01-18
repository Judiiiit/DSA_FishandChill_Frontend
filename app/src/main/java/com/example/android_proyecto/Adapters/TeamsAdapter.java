package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.TeamRanking;
import com.example.android_proyecto.R;

import java.util.List;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.VH> {

    public interface OnTeamActionListener {
        void onOpenMembers(TeamRanking team);
        void onJoin(TeamRanking team);
    }

    private final List<TeamRanking> teams;
    private final String currentTeamName;
    private final OnTeamActionListener listener;

    public TeamsAdapter(List<TeamRanking> teams, String currentTeamName, OnTeamActionListener listener) {
        this.teams = teams;
        this.currentTeamName = currentTeamName;
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
        TeamRanking t = teams.get(position);

        int rank = position + 1;
        String label = "#" + rank + "  " + (t.getName() != null ? t.getName() : "-");
        label = label + "  (" + t.getPoints() + ")";
        holder.tvGroupName.setText(label);

        boolean hasTeam = currentTeamName != null && !currentTeamName.isEmpty() && !"None".equalsIgnoreCase(currentTeamName);
        boolean isMyTeam = hasTeam && t.getName() != null && currentTeamName.equalsIgnoreCase(t.getName());

        if (isMyTeam) {
            holder.btnJoinGroup.setText("My team");
            holder.btnJoinGroup.setEnabled(false);
            holder.layoutGroup.setAlpha(0.75f);
        } else if (hasTeam) {
            holder.btnJoinGroup.setText("In team");
            holder.btnJoinGroup.setEnabled(false);
            holder.layoutGroup.setAlpha(1.0f);
        } else {
            holder.btnJoinGroup.setText("Join");
            holder.btnJoinGroup.setEnabled(true);
            holder.layoutGroup.setAlpha(1.0f);
        }

        holder.btnJoinGroup.setOnClickListener(v -> {
            if (listener != null) listener.onJoin(t);
        });

        holder.layoutGroup.setOnClickListener(v -> {
            if (listener != null) listener.onOpenMembers(t);
        });
    }

    @Override
    public int getItemCount() {
        return teams != null ? teams.size() : 0;
    }
}
