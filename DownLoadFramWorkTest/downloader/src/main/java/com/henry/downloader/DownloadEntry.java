package com.henry.downloader;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

@Entity(tableName = "downloadEntry")
public class DownloadEntry implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.TEXT)
    @NonNull
    public String id;
    @ColumnInfo(name = "name", typeAffinity = ColumnInfo.TEXT)
    public String name;
    @ColumnInfo(name = "url", typeAffinity = ColumnInfo.TEXT)
    public String url;
    @ColumnInfo(name = "status", typeAffinity = ColumnInfo.TEXT)
    public DownLoadStatus status = DownLoadStatus.idle;
    @ColumnInfo(name = "currentLength", typeAffinity = ColumnInfo.INTEGER)
    public int currentLength;
    @ColumnInfo(name = "totalLength", typeAffinity = ColumnInfo.INTEGER)
    public int totalLength;
    @ColumnInfo(name = "isSupportRange", typeAffinity = ColumnInfo.INTEGER)
    public boolean isSupportRange;
    @ColumnInfo(name = "ranges")
    public HashMap<Integer, Integer> ranges;
    @ColumnInfo(name = "percent")
    public int percent;

    @Ignore
    public DownloadEntry() {
    }

    @Ignore
    public DownloadEntry(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }


    public DownloadEntry(String id, String name, String url, DownLoadStatus status, int currentLength, int totalLength) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.status = status;
        this.currentLength = currentLength;
        this.totalLength = totalLength;
    }

    public void reset() {
        currentLength = 0;
        ranges = null;
        percent = 0;
    }

    public enum DownLoadStatus {waiting, downloading, paused, resume, canceled, idle, connecting, error, completed}


    @Override
    public String toString() {
        return "DownloadEntry{" +
                "status=" + status +
                ", currentLength=" + currentLength +
                ", totalLength=" + totalLength +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
