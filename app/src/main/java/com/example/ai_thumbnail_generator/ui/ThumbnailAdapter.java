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

    public interface OnThumbnailInteractionListener {
        void onShareToggled(Models.ThumbnailData item);
        void onDeleteClicked(Models.ThumbnailData item);
        void onCardClicked(Models.ThumbnailData item);
        void onDownloadClicked(Models.ThumbnailData item);
        void onLikeClicked(Models.ThumbnailData item, boolean isLiked);
    }

    private List<Models.ThumbnailData> items = new ArrayList<>();
    private String currentUserId;
    private OnThumbnailInteractionListener listener;
    private boolean showDeleteButton = true;
    private boolean showShareButton = true;

    public void setItems(List<Models.ThumbnailData> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setOnThumbnailInteractionListener(OnThumbnailInteractionListener listener) {
        this.listener = listener;
    }

    public void setShowDeleteButton(boolean showDeleteButton) {
        this.showDeleteButton = showDeleteButton;
    }

    public void setShowShareButton(boolean showShareButton) {
        this.showShareButton = showShareButton;
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
        
        // Set beautifully formatted relative time span
        holder.tvDate.setText(getRelativeTime(item.created_at));

        // Bind Creator profile details dynamically
        if (item.profiles != null) {
            holder.tvCreatorName.setText(item.profiles.display_name);
            if (item.profiles.avatar_url != null && !item.profiles.avatar_url.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.profiles.avatar_url)
                        .circleCrop()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .into(holder.ivCreatorAvatar);
            } else {
                holder.ivCreatorAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }
        } else {
            holder.tvCreatorName.setText("Anonymous");
            holder.ivCreatorAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        // Set dynamic aspect ratio constraint programmatically
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) holder.ivThumbnail.getLayoutParams();
        if ("1:1".equals(item.ratio_type)) {
            params.dimensionRatio = "H,1:1";
        } else if ("9:16".equals(item.ratio_type)) {
            params.dimensionRatio = "H,9:16";
        } else {
            params.dimensionRatio = "H,16:9";
        }
        holder.ivThumbnail.setLayoutParams(params);

        // Dynamically style the Hook Text Ribbon with Leonardo palette
        if (item.hook_text != null && !item.hook_text.isEmpty()) {
            holder.viewRibbon.setVisibility(View.VISIBLE);
            holder.tvHookText.setVisibility(View.VISIBLE);
            holder.tvHookText.setText(item.hook_text.toUpperCase());
            
            if (item.color_palette != null && !item.color_palette.isEmpty()) {
                try {
                    holder.viewRibbon.setBackgroundColor(android.graphics.Color.parseColor(item.color_palette));
                } catch (Exception e) {
                    holder.viewRibbon.setBackgroundColor(android.graphics.Color.parseColor("#2563EB")); // Fallback primary
                }
            } else {
                holder.viewRibbon.setBackgroundColor(android.graphics.Color.parseColor("#2563EB"));
            }
        } else {
            holder.viewRibbon.setVisibility(View.GONE);
            holder.tvHookText.setVisibility(View.GONE);
        }

        // Always display actions row so all users can save/download public items
        holder.layoutActions.setVisibility(View.VISIBLE);

        boolean isOwner = currentUserId != null && currentUserId.equals(item.user_id);
        if (isOwner) {
            holder.btnShare.setVisibility(showShareButton ? View.VISIBLE : View.GONE);
            holder.btnDelete.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

            if (item.is_public) {
                holder.btnShare.setColorFilter(android.graphics.Color.parseColor("#10B981")); // Emerald green
            } else {
                holder.btnShare.setColorFilter(android.graphics.Color.parseColor("#94A3B8")); // Slate gray
            }

            holder.btnShare.setOnClickListener(v -> {
                if (listener != null) listener.onShareToggled(item);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClicked(item);
            });
        } else {
            // Non-owners can only save/download, they can't publish or delete
            holder.btnShare.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Set Save/Download click handler for all users
        holder.btnDownload.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadClicked(item);
            }
        });

        // Bind Like state and click interactively
        com.example.ai_thumbnail_generator.utils.SessionManager sessionManager = new com.example.ai_thumbnail_generator.utils.SessionManager(holder.itemView.getContext());
        boolean isLiked = sessionManager.isLiked(item.id);
        
        holder.tvLikeCount.setText(String.valueOf(item.likes));
        if (isLiked) {
            holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
            holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#EF4444")); // Premium Red tint
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart);
            holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#94A3B8")); // Premium Slate tint
        }

        holder.layoutLike.setOnClickListener(v -> {
            boolean currentLiked = sessionManager.isLiked(item.id);
            boolean newLiked = !currentLiked;
            sessionManager.setLiked(item.id, newLiked);

            // Optimistic UI updates
            int countDiff = newLiked ? 1 : -1;
            item.likes = Math.max(0, item.likes + countDiff);
            holder.tvLikeCount.setText(String.valueOf(item.likes));

            if (newLiked) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart);
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#94A3B8"));
            }

            if (listener != null) {
                listener.onLikeClicked(item, newLiked);
            }
        });

        // Load visual illustration background
        Glide.with(holder.itemView.getContext())
                .load(item.image_url)
                .into(holder.ivThumbnail);

        // Bind global card click to show full image preview
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        View viewRibbon;
        TextView tvHookText;
        TextView tvTitle, tvRatio, tvDate;
        View layoutActions;
        ImageView btnShare;
        ImageView btnDownload;
        ImageView btnDelete;
        ImageView ivCreatorAvatar;
        TextView tvCreatorName;
        View layoutLike;
        ImageView btnLike;
        TextView tvLikeCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            viewRibbon = itemView.findViewById(R.id.viewRibbon);
            tvHookText = itemView.findViewById(R.id.tvHookText);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRatio = itemView.findViewById(R.id.tvRatio);
            tvDate = itemView.findViewById(R.id.tvDate);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivCreatorAvatar = itemView.findViewById(R.id.ivCreatorAvatar);
            tvCreatorName = itemView.findViewById(R.id.tvCreatorName);
            layoutLike = itemView.findViewById(R.id.layoutLike);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }
    }

    private static String getRelativeTime(String isoString) {
        if (isoString == null || isoString.isEmpty()) return "";
        
        try {
            // Check Android version to use modern java.time APIs
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(isoString);
                long timeMs = odt.toInstant().toEpochMilli();
                long nowMs = System.currentTimeMillis();
                long diffMs = nowMs - timeMs;
                
                if (diffMs < 0) diffMs = 0;
                
                long diffSec = diffMs / 1000;
                long diffMin = diffSec / 60;
                long diffHr = diffMin / 60;
                long diffDay = diffHr / 24;
                
                if (diffSec < 60) {
                    return "Just now";
                } else if (diffMin < 60) {
                    return diffMin == 1 ? "1 min ago" : diffMin + " mins ago";
                } else if (diffHr < 24) {
                    return diffHr == 1 ? "1 hour ago" : diffHr + " hours ago";
                } else if (diffDay < 7) {
                    if (diffDay == 1) return "Yesterday";
                    return diffDay + " days ago";
                } else {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    return odt.format(dtf);
                }
            } else {
                // Legacy support using SimpleDateFormat
                String clean = isoString.split("\\+")[0].split("Z")[0];
                if (clean.contains(".")) {
                    clean = clean.split("\\.")[0];
                }
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = sdf.parse(clean);
                if (date == null) return isoString;
                
                long timeMs = date.getTime();
                long nowMs = System.currentTimeMillis();
                long diffMs = nowMs - timeMs;
                
                if (diffMs < 0) diffMs = 0;
                
                long diffSec = diffMs / 1000;
                long diffMin = diffSec / 60;
                long diffHr = diffMin / 60;
                long diffDay = diffHr / 24;
                
                if (diffSec < 60) {
                    return "Just now";
                } else if (diffMin < 60) {
                    return diffMin == 1 ? "1 min ago" : diffMin + " mins ago";
                } else if (diffHr < 24) {
                    return diffHr == 1 ? "1 hour ago" : diffHr + " hours ago";
                } else if (diffDay < 7) {
                    if (diffDay == 1) return "Yesterday";
                    return diffDay + " days ago";
                } else {
                    java.text.SimpleDateFormat outSdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                    return outSdf.format(date);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Return simple date string fallback (yyyy-MM-dd)
                return isoString.split("T")[0];
            } catch (Exception ex) {
                return isoString;
            }
        }
    }
}
