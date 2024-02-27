package com.henry.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.henry.downloader.db.DownloadEntryDao;
import com.henry.downloader.db.DownloadEntryDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class DownloadService extends Service {
    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSE_OR_CANCEL = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_CONNECTING = 5;
    //    1. net error 2. no sd 3. no memory
    public static final int NOTIFY_ERROR = 6;

    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap<>();
    private ExecutorService mExecutors;
    private LinkedBlockingDeque<DownloadEntry> mWaitingQueue = new LinkedBlockingDeque<>();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            DownloadEntry entry = (DownloadEntry) msg.obj;
            switch (msg.what) {
                case NOTIFY_COMPLETED:
                case NOTIFY_PAUSE_OR_CANCEL:
                case NOTIFY_ERROR:
                    mDownloadingTasks.remove(entry.id);
                    checkNext();
            }
            DataChanger.getInstance(getApplicationContext()).postStatus((DownloadEntry) msg.obj);
        }
    };
    private DataChanger mDataChanger;
    private DownloadEntryDao mDownloadEntryDao;

    private void checkNext() {
        DownloadEntry newEntry = mWaitingQueue.poll();
        if (newEntry != null) {
            startDownLoad(newEntry);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
        mDataChanger = DataChanger.getInstance(getApplicationContext());
        mDownloadEntryDao = DownloadEntryDatabase.getInstance(getApplicationContext()).downloadEntryDao();

        // FIXME need to use liveData
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DownloadEntry> downloadEntries = mDownloadEntryDao.queryAll();
                if (downloadEntries != null) {
                    for (DownloadEntry entry : downloadEntries) {
                        if (entry.status == DownloadEntry.DownLoadStatus.downloading || entry.status == DownloadEntry.DownLoadStatus.waiting) {
                            entry.status = DownloadEntry.DownLoadStatus.paused;
                            // TODO add a config if need to recover download
                            addDownload(entry);
                        }
                        mDataChanger.addToOperatedEntryMap(entry.id, entry);
                    }
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Trace.e("onStartCommand");
        if (intent != null) {
            DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            if (entry != null) {
                if (mDataChanger.containsDownloadEntry(entry.id)) {
                    entry = mDataChanger.queryDownloadEntryById(entry.id);
                }
                int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
                doAction(action, entry);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        // check action, do related action
        Trace.e("doAction");
        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
//                startDownLoad(entry);
                addDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownLoad(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownLoad(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownLoad(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
        }
    }

    private void recoverAll() {
        ArrayList<DownloadEntry> recoverableEntries = DataChanger.getInstance(getApplicationContext()).queryAllRecoverableEntries();
        if (recoverableEntries != null) {
            for (DownloadEntry entry : recoverableEntries) {
                addDownload(entry);
            }
        }
    }

    private void pauseAll() {
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry entry = mWaitingQueue.poll();
            entry.status = DownloadEntry.DownLoadStatus.paused;
            // FIXME notify all once
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }

        for (Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }
        mDownloadingTasks.clear();
    }

    private void addDownload(DownloadEntry entry) {
        Trace.e("addDownload (mDownloadingTasks.size(): " + mDownloadingTasks.size());
        if (mDownloadingTasks.size() >= Constants.MAX_DOWNLOAD_TASKS) {
            mWaitingQueue.offer(entry);
            entry.status = DownloadEntry.DownLoadStatus.waiting;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        } else {
            startDownLoad(entry);
        }
    }

    private void cancelDownLoad(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownLoadStatus.canceled;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    }

    private void resumeDownLoad(DownloadEntry entry) {
        addDownload(entry);
    }

    private void pauseDownLoad(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownLoadStatus.paused;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    }

    private void startDownLoad(DownloadEntry entry) {
        Trace.e("startDownLoad");
        DownloadTask task = new DownloadTask(entry, mHandler, mExecutors);
        task.start();
//        mExecutors.execute(task);
        mDownloadingTasks.put(entry.id, task);
    }
}
