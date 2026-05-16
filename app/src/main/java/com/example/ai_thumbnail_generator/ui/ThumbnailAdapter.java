package com.example.ai_thumbnail_generator.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_thumbnail_generator.R;
import com.example.ai_thumbnail_generator.network.Models;

import java.util.ArrayList;
import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

    private List<Models.ThumbnailData> items = new ArrayList<>();

    private final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());

    public void setItems(List<Models.ThumbnailData> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.ThumbnailData item = items.get(position);
        holder.tvTitle.setText(item.original_title);
        holder.tvRatio.setText(item.ratio_type);
        
        try {
            // ISO format usually looks like: 2026-05-16T13:47:46.000Z
            String cleanDate = item.created_at.replace("T", " ").split("\\.")[0];
            holder.tvDate.setText(cleanDate);
        } catch (Exception e) {
            holder.tvDate.setText(item.created_at);
        }

        Glide.with(holder.itemView.getContext())
                .load(item.image_url)
                .into(holder.ivThumbnail);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvRatio, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRatio = itemView.findViewById(R.id.tvRatio);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
