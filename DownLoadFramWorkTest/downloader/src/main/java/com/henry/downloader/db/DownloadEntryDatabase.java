package com.henry.downloader.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.henry.downloader.DownloadEntry;

@Database(entities = {DownloadEntry.class}, version = 1)
@TypeConverters({Converter.class, RangeConverter.class})
public abstract class DownloadEntryDatabase extends RoomDatabase {
    private static final String DB_NAME = "db_downloader";
    private static volatile DownloadEntryDatabase instance;

    public static DownloadEntryDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadEntryDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DownloadEntryDatabase.class,
                            DB_NAME)
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract DownloadEntryDao downloadEntryDao();
}
