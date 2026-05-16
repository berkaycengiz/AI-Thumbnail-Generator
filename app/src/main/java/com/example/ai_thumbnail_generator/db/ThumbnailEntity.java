package com.example.ai_thumbnail_generator.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "thumbnails")
public class ThumbnailEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String originalTitle;
    public String hookText;
    public String colorPalette;
    public String localUri;
    public String ratioType;
    public long date;

    public ThumbnailEntity(String originalTitle, String hookText, String colorPalette, String localUri, String ratioType, long date) {
        this.originalTitle = originalTitle;
        this.hookText = hookText;
        this.colorPalette = colorPalette;
        this.localUri = localUri;
        this.ratioType = ratioType;
        this.date = date;
    }
}
