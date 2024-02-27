package com.henry.downloader;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {
    private final DownloadEntry entry;
    private final Handler mHandler;
    private final ExecutorService mExecutor;
    private volatile boolean isPaused;
    private volatile boolean isCanceled;
    private ConnectThread mConnectThread;
    private DownloadThread[] mDownloadThreads;

    public DownloadTask(DownloadEntry entry, Handler mHandler, ExecutorService executor) {
        this.entry = entry;
        this.mHandler = mHandler;
        this.mExecutor = executor;
    }

    public void pause() {
        Trace.e("DownloadTask pause");
        isPaused = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    if (entry.isSupportRange) {
                        mDownloadThreads[i].pause();
                    } else {
                        mDownloadThreads[i].cancel();
                    }
                }
            }
        }
    }

    public void cancel() {
        Trace.e("DownloadTask cancel");
        isCanceled = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    mDownloadThreads[i].cancel();
                }
            }
        }
    }

    public void start() {
        if (entry.totalLength > 0) {
            Trace.e("no need to check if support range and totalLength");
            startDownload();
        } else {
            entry.status = DownloadEntry.DownLoadStatus.connecting;
            notifyUpdate(entry, DownloadService.NOTIFY_CONNECTING);
            mConnectThread = new ConnectThread(entry.url, this);
            mExecutor.execute(mConnectThread);
        }
    }

    private void startDownload() {
//        entry.isSupportRange = false;
        if (entry.isSupportRange) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void notifyUpdate(DownloadEntry entry, int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = entry;
        mHandler.sendMessage(msg);

    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        entry.isSupportRange = isSupportRange;
        entry.totalLength = totalLength;
        startDownload();
    }

    private void startMultiDownload() {
        entry.status = DownloadEntry.DownLoadStatus.downloading;
        notifyUpdate(entry, DownloadService.NOTIFY_DOWNLOADING);
        int block = entry.totalLength / Constants.MAX_DOWNLOAD_THREADS;
        int startPos = 0;
        int endPos = 0;
        if (entry.ranges == null) {
            entry.ranges = new HashMap<>();
            for (int i = 0; i < Constants.MAX_DOWNLOAD_THREADS; i++) {
                entry.ranges.put(i, 0);
            }
        }
        mDownloadThreads = new DownloadThread[Constants.MAX_DOWNLOAD_THREADS];
        for (int i = 0; i < Constants.MAX_DOWNLOAD_THREADS; i++) {
            startPos = i * block + entry.ranges.get(i);
            // FIXME change MAX_DOWNLOAD_TASKS -> MAX_DOWNLOAD_THREADS
            if (i == Constants.MAX_DOWNLOAD_TASKS - 1) {
                endPos = entry.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(entry.url, i, startPos, endPos, this);
                mExecutor.execute(mDownloadThreads[i]);
            }
        }
    }

    private void startSingleDownload() {
        entry.status = DownloadEntry.DownLoadStatus.downloading;
        notifyUpdate(entry, DownloadService.NOTIFY_DOWNLOADING);
        mDownloadThreads = new DownloadThread[1];

        mDownloadThreads[0] = new DownloadThread(entry.url, 0, -1, -1, this);
        mExecutor.execute(mDownloadThreads[0]);
    }

    @Override
    public synchronized void onConnectError(String message) {
        Trace.e("onError, " + message);
        if (isPaused || isCanceled) {
            entry.status = isPaused ? DownloadEntry.DownLoadStatus.paused : DownloadEntry.DownLoadStatus.canceled;
            notifyUpdate(entry, DownloadService.NOTIFY_PAUSE_OR_CANCEL);
        } else {
            entry.status = DownloadEntry.DownLoadStatus.error;
            notifyUpdate(entry, DownloadService.NOTIFY_ERROR);
        }
    }

    @Override
    public synchronized void onProgressChanged(int index, int progress) {
        if (entry.isSupportRange) {
            int range = entry.ranges.get(index) + progress;
            entry.ranges.put(index, range);
        }

        entry.currentLength += progress;
        if (entry.currentLength >= entry.totalLength) {
            entry.percent = 100;
            entry.status = DownloadEntry.DownLoadStatus.completed;
            notifyUpdate(entry, DownloadService.NOTIFY_COMPLETED);
        } else {
            int percent = (int) (entry.currentLength * 100L / entry.totalLength);
//            Trace.e("percent: " + percent);
            if (percent > entry.percent) {
                entry.percent = percent;
                notifyUpdate(entry, DownloadService.NOTIFY_UPDATING);
            }

        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {

    }

    @Override
    public synchronized void onDownloadError(int index, String message) {
        Trace.e("onDownloadError: " + message);
        boolean isAllError = true;
        for (int i = 0; i < mDownloadThreads.length; i++) {
            if (mDownloadThreads[i] != null) {
                if (!mDownloadThreads[i].isError()) {
                    isAllError = false;
                    mDownloadThreads[i].cancelByError();
                }
            }
        }
        if (isAllError) {
            entry.status = DownloadEntry.DownLoadStatus.error;
            notifyUpdate(entry, DownloadService.NOTIFY_ERROR);
        }
    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        for (int i = 0; i < mDownloadThreads.length; i++) {
            if (mDownloadThreads[i] != null) {
                if (!mDownloadThreads[i].isPaused()) {
                    return;
                }
            }
        }
        entry.status = DownloadEntry.DownLoadStatus.paused;
        notifyUpdate(entry, DownloadService.NOTIFY_PAUSE_OR_CANCEL);
    }

    @Override
    public synchronized void onDownloadCanceled(int index) {
        for (int i = 0; i < mDownloadThreads.length; i++) {
            if (mDownloadThreads[i] != null) {
                if (!mDownloadThreads[i].isCanceled()) {
                    return;
                }
            }
        }
        entry.status = DownloadEntry.DownLoadStatus.canceled;
        entry.reset();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "henry" + entry.url.substring(entry.url.lastIndexOf("/") + 1);
        File file = new File(path);
        if (file.exists())
            file.delete();
        notifyUpdate(entry, DownloadService.NOTIFY_PAUSE_OR_CANCEL);
    }

    //    TODO 1.check if support rage, get content-length
//    TODO 2.if not, single thread to download. can't be paused|resume.
//    TODO 3.if support, multiple thread to download
//    TODO 3.1 compute the block size per thread
//    TODO 3.2 execute sub-thread
//    TODO 3.3 combine the progress and notify

}
