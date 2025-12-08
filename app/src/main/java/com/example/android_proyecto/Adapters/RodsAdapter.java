package com.example.android_proyecto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_proyecto.Models.FishingRod;
import com.example.android_proyecto.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RodsAdapter extends RecyclerView.Adapter<RodsAdapter.RodViewHolder> {

    public interface OnRodClickListener {
        void onBuyClick(FishingRod rod);
    }

    private List<FishingRod> rods;
    private OnRodClickListener listener;

    private Set<String> ownedRodNames;
    private boolean inventoryMode = false;

    public RodsAdapter(List<FishingRod> rods,
                       OnRodClickListener listener,
                       Set<String> ownedRodNames) {
        this.rods = rods;
        this.listener = listener;
        this.ownedRodNames = ownedRodNames != null ? ownedRodNames : new HashSet<>();
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

        boolean isOwned = ownedRodNames != null
                && rod.getName() != null
                && ownedRodNames.contains(rod.getName());

        if (inventoryMode) {
            // MODO INVENTARIO: solo visualizar, sin opción de compra
            holder.btnBuy.setVisibility(View.GONE);
            holder.btnBuy.setOnClickListener(null);
        } else {
            // MODO TIENDA: se muestra el botón
            holder.btnBuy.setVisibility(View.VISIBLE);

            if (isOwned) {
                holder.btnBuy.setEnabled(false);
                holder.btnBuy.setAlpha(0.3f);          // se ve “apagado”
                holder.btnBuy.setOnClickListener(null);
            } else {
                holder.btnBuy.setEnabled(true);
                holder.btnBuy.setAlpha(1.0f);
                holder.btnBuy.setOnClickListener(v -> {
                    if (listener != null) listener.onBuyClick(rod);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return rods != null ? rods.size() : 0;
    }

    public void setRods(List<FishingRod> rods) {
        this.rods = rods;
        notifyDataSetChanged();
    }

    public void setOwnedRodNames(Set<String> ownedRodNames) {
        this.ownedRodNames = ownedRodNames != null ? ownedRodNames : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setInventoryMode(boolean inventoryMode) {
        this.inventoryMode = inventoryMode;
        notifyDataSetChanged();
    }

    static class RodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;
        ImageButton btnBuy;

        RodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRodName);
            tvDesc = itemView.findViewById(R.id.tvRodDesc);
            tvPrice = itemView.findViewById(R.id.tvRodPrice);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}
