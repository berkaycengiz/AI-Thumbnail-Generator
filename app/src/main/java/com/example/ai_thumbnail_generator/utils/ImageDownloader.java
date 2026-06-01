package com.example.ai_thumbnail_generator.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDownloader {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void download(Context context, String imageUrl, String title) {
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                // Fetch full resolution background bitmap via Glide
                Bitmap bitmap = Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();

                boolean success = saveImageToGallery(context, bitmap, title);

                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(context, "Saved to Gallery! Check your Photos app.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(context, "Error downloading: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static boolean saveImageToGallery(Context context, Bitmap bitmap, String title) {
        OutputStream fos;
        Uri imageUri = null;
        String fileName = "AI_Thumbnail_" + System.currentTimeMillis() + ".jpg";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AI_Thumbnails");

                imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri == null) return false;
                fos = context.getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File myDir = new java.io.File(imagesDir + "/AI_Thumbnails");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                java.io.File file = new java.io.File(myDir, fileName);
                fos = new java.io.FileOutputStream(file);
                
                // Add index record to media database
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
