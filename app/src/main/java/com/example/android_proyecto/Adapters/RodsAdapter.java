package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.R;

import java.util.List;

public class RodsAdapter extends RecyclerView.Adapter<RodsAdapter.RodViewHolder> {

    public interface OnRodClickListener {
        void onBuyClick(FishingRod rod);
    }

    private List<FishingRod> rods;
    private OnRodClickListener listener;

    public RodsAdapter(List<FishingRod> rods, OnRodClickListener listener) {
        this.rods = rods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rod, parent, false);
        return new RodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RodViewHolder holder, int position) {
        FishingRod rod = rods.get(position);

        holder.tvName.setText(rod.getName());

        // Aquí mostramos info con los campos reales del backend
        String desc = "Speed: " + rod.getSpeed()
                + "  Power: " + rod.getPower()
                + "  Rarity: " + rod.getRarity();
        holder.tvDesc.setText(desc);

        holder.tvPrice.setText("Price: " + rod.getPrice());

        holder.btnBuy.setOnClickListener(v -> {
            if (listener != null) listener.onBuyClick(rod);
        });
    }

    @Override
    public int getItemCount() {
        return rods != null ? rods.size() : 0;
    }

    public void setRods(List<FishingRod> rods) {
        this.rods = rods;
        notifyDataSetChanged();
    }

    static class RodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;
        Button btnBuy;

        RodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRodName);
            tvDesc = itemView.findViewById(R.id.tvRodDesc);
            tvPrice = itemView.findViewById(R.id.tvRodPrice);
            btnBuy = itemView.findViewById(R.id.btnBuyRod);
        }
    }
}
