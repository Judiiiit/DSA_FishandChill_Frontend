package com.example.android_proyecto.Adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_proyecto.Models.Video;
import com.example.android_proyecto.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videos;
    private Context context;

    public VideoAdapter(List<Video> videos) {
        this.videos = videos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.tvTitle.setText(video.getTitle());

        // CAMBIO: Usamos directamente el getUrl() como ID
        String videoId = video.extractVideoIdFromUrl(video.getUrl());

        if (videoId != null && !videoId.isEmpty()) {

            // 1. Crear URL de la miniatura
            String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";

            // 2. Cargar imagen
            Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.imgThumbnail);

            // 3. Configurar Click
            holder.itemView.setOnClickListener(v -> openYoutubeVideo(videoId));
        }
    }

    @Override
    public int getItemCount() {
        return videos != null ? videos.size() : 0;
    }

    private void openYoutubeVideo(String videoId) {
        try {
            // Intenta abrir la App de YouTube
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            // Si no tiene la app, abre el navegador
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
            context.startActivity(webIntent);
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imgThumbnail;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
        }
    }
}