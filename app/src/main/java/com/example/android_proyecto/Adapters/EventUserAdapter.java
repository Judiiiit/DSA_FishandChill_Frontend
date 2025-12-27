package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Models.EventUser;
import com.example.android_proyecto.R;

import java.util.List;

public class EventUserAdapter extends RecyclerView.Adapter<EventUserAdapter.VH> {

    private final List<EventUser> users;

    public EventUserAdapter(List<EventUser> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        EventUser u = users.get(position);
        holder.tvName.setText(u.getName());
        holder.tvSurnames.setText(u.getSurnames());

        Glide.with(holder.itemView.getContext())
                .load(u.getAvatar())
                .centerCrop()
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvSurnames;

        public VH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvSurnames = itemView.findViewById(R.id.tvSurnames);
        }
    }
}
