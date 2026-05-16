package com.example.ai_thumbnail_generator.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class ThumbnailRenderer {

    public static Bitmap draw(Context ctx, Bitmap aiBg, String text, String hexColor, String ratioType) {
        int targetWidth, targetHeight;

        switch (ratioType) {
            case "16:9":
                targetWidth = 1280;
                targetHeight = 720;
                break;
            case "1:1":
                targetWidth = 1080;
                targetHeight = 1080;
                break;
            case "9:16":
                targetWidth = 1080;
                targetHeight = 1920;
                break;
            default:
                targetWidth = 1280;
                targetHeight = 720;
        }

        Bitmap scaledBg = scaleCenterCrop(aiBg, targetWidth, targetHeight);
        Bitmap result = scaledBg.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        Paint overlayPaint = new Paint();
        try {
            overlayPaint.setColor(Color.parseColor(hexColor));
        } catch (Exception e) {
            overlayPaint.setColor(Color.BLUE);
        }
        overlayPaint.setAlpha(180);

        int ribbonHeight = targetHeight / 4;
        canvas.drawRect(0, targetHeight - ribbonHeight, targetWidth, targetHeight, overlayPaint);

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(targetWidth / 15f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setShadowLayer(10, 5, 5, Color.BLACK);

        int padding = 40;
        StaticLayout staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, targetWidth - (padding * 2))
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0, 1.2f)
                .setIncludePad(true)
                .build();

        canvas.save();
        float textX = padding;
        float textY = (targetHeight - ribbonHeight) + (ribbonHeight / 2f) - (staticLayout.getHeight() / 2f);
        canvas.translate(textX, textY);
        staticLayout.draw(canvas);
        canvas.restore();

        return result;
    }

    private static Bitmap scaleCenterCrop(Bitmap source, int newWidth, int newHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        Rect targetRect = new Rect((int) left, (int) top, (int) (left + scaledWidth), (int) (top + scaledHeight));
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);
        return dest;
    }
}
