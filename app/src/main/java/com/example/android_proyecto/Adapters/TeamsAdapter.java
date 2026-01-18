package com.example.android_proyecto.Adapters;

import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Models.TeamRanking;
import com.example.android_proyecto.R;
import com.example.android_proyecto.Services.SessionManager;
import com.example.android_proyecto.glide.SvgSoftwareLayerSetter;

import java.util.List;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.VH> {

    public interface OnTeamActionListener {
        void onOpenMembers(TeamRanking team);
        void onJoin(TeamRanking team);
    }

    private final List<TeamRanking> teams;
    private final String currentTeamName;
    private final OnTeamActionListener listener;
    private final SessionManager session; // ✅ para cachear avatar

    public TeamsAdapter(List<TeamRanking> teams, String currentTeamName, OnTeamActionListener listener, SessionManager session) {
        this.teams = teams;
        this.currentTeamName = currentTeamName;
        this.listener = listener;
        this.session = session;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        Button btnJoinGroup;
        LinearLayout layoutGroup;
        ImageView imgTeamAvatar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            btnJoinGroup = itemView.findViewById(R.id.btnJoinGroup);
            layoutGroup = itemView.findViewById(R.id.layoutGroup);
            imgTeamAvatar = itemView.findViewById(R.id.imgTeamAvatar);
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

        String teamName = t.getName();
        String url = t.getAvatar();

        if (session != null && teamName != null && url != null && !url.trim().isEmpty()) {
            session.saveTeamAvatarUrl(teamName, url);
        }

        Glide.with(holder.itemView.getContext()).clear(holder.imgTeamAvatar);
        holder.imgTeamAvatar.setImageDrawable(null);

        if (url == null || url.trim().isEmpty()) {
            holder.imgTeamAvatar.setImageResource(R.drawable.avatar_1);
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
                    .into(holder.imgTeamAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .centerCrop()
                    .into(holder.imgTeamAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return teams != null ? teams.size() : 0;
    }
}
