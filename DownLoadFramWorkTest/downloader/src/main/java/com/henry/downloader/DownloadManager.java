package com.henry.downloader;

import android.content.Context;
import android.content.Intent;

public class DownloadManager {
    private static volatile DownloadManager instance;
    private final Context context;
    private static final int MIN_OPERATE_INTERVAL = 1000 * 1;
    private long mLastOperatedTime = 0;

    private DownloadManager(Context context) {
        this.context = context;
        context.startService(new Intent(context, DownloadService.class));
    }

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }
        return instance;
    }

    public void add(DownloadEntry entry) {
        Trace.e("add");
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    private boolean checkIfExecutable() {
        long tmp = System.currentTimeMillis();
        if (tmp - mLastOperatedTime > MIN_OPERATE_INTERVAL) {
            mLastOperatedTime = tmp;
            return true;
        }
        return false;
    }

    public void pause(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    public void resume(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    public void cancel(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void pauseAll() {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
//        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        context.startService(intent);
    }

    public void recoverAll() {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
//        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        context.startService(intent);
    }


    public void addObserver(DataWatcher watcher) {
        Trace.e("addObserver");
        DataChanger.getInstance(context).addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        DataChanger.getInstance(context).deleteObserver(watcher);
    }

    public DownloadEntry queryDownloadEntry(String id) {
        return DataChanger.getInstance(context).queryDownloadEntryById(id);
    }
}
