package com.henry.downloader;

import android.content.Context;

import com.henry.downloader.db.DownloadEntryDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DataChanger extends Observable {
    private static volatile DataChanger instance;
    private final Context context;
    private LinkedHashMap<String, DownloadEntry> mOperatedEntries;
    // FIXME to Change
    private ExecutorService executorService;

    private DataChanger(Context context) {
        mOperatedEntries = new LinkedHashMap<>();
        this.context = context;
        executorService = Executors.newCachedThreadPool();
    }

    public static DataChanger getInstance(Context context) {
        if (instance == null) {
            synchronized (DataChanger.class) {
                if (instance == null) {
                    instance = new DataChanger(context);
                }
            }
        }
        return instance;
    }


    public void postStatus(DownloadEntry entry) {
        mOperatedEntries.put(entry.id, entry);
//        DownloadEntryDatabase.getInstance(context).downloadEntryDao().insertEntry(entry);
        // FIXME need to use liveData
        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadEntryDatabase.getInstance(context).downloadEntryDao().insertEntry(entry);
            }
        }).start();
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                DownloadEntryDatabase.getInstance(context).downloadEntryDao().insertEntry(entry);
//            }
//        });
        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> recoverableEntries = null;
        for (Map.Entry<String, DownloadEntry> entry : mOperatedEntries.entrySet()) {
            if (entry.getValue().status == DownloadEntry.DownLoadStatus.paused) {
                if (recoverableEntries == null) {
                    recoverableEntries = new ArrayList<>();
                }
                recoverableEntries.add(entry.getValue());
            }
        }
        return recoverableEntries;
    }

    public DownloadEntry queryDownloadEntryById(String id) {
        return mOperatedEntries.get(id);
    }

    public void addToOperatedEntryMap(String key, DownloadEntry value) {
        mOperatedEntries.put(key, value);
    }

    public boolean containsDownloadEntry(String id) {
        return mOperatedEntries.containsKey(id);
    }
}
