package com.henry.downloader.db;

import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.henry.downloader.DownloadEntry;

import java.util.HashMap;

public class Converter {
    @TypeConverter
    public String fromStatus(DownloadEntry.DownLoadStatus status) {
        return status.name();
    }

    @TypeConverter
    public DownloadEntry.DownLoadStatus toStatus(String status) {
        return DownloadEntry.DownLoadStatus.valueOf(status);
    }
}
