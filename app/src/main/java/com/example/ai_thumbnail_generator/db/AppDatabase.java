package com.example.ai_thumbnail_generator.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ThumbnailEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ThumbnailDao thumbnailDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "thumbnail_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
