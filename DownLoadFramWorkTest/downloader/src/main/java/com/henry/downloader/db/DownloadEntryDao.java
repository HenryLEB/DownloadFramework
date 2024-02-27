package com.henry.downloader.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.henry.downloader.DownloadEntry;

import java.util.List;

@Dao
public interface DownloadEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntry(DownloadEntry entry);

    @Delete
    void deleteEntry(DownloadEntry entry);

    @Update
    void updateEntry(DownloadEntry entry);

    @Query("SELECT * FROM downloadEntry")
    List<DownloadEntry> queryAll();

    @Query("SELECT * FROM downloadEntry WHERE id=:id")
    DownloadEntry queryEntryById(String id);
}
