package com.example.ai_thumbnail_generator.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ThumbnailDao {
    @Insert
    void insert(ThumbnailEntity entity);

    @Query("SELECT * FROM thumbnails ORDER BY date DESC")
    LiveData<List<ThumbnailEntity>> getAllThumbnails();
}
