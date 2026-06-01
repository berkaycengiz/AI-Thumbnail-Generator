package com.example.ai_thumbnail_generator.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.ai_thumbnail_generator.R;

public class ImageDetailDialog {

    public static void show(Context context, String imageUrl) {
        // Create full screen dialog matching system theme
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Keep custom translucent background
        }

        dialog.setContentView(R.layout.dialog_full_image);
        dialog.setCancelable(true);

        ImageView ivFull = dialog.findViewById(R.id.ivFullImage);
        View btnClose = dialog.findViewById(R.id.btnDialogClose);
        View bgClose = dialog.findViewById(R.id.background_close_trigger);

        // Load full fidelity image via Glide
        Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .placeholder(android.R.drawable.progress_horizontal)
                .into(ivFull);

        // Multiple close triggers for smooth UX
        btnClose.setOnClickListener(v -> dialog.dismiss());
        bgClose.setOnClickListener(v -> dialog.dismiss());
        ivFull.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
