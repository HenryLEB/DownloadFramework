package com.henry.downloader;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread implements Runnable {
    private final int endPos;
    private final int startPos;
    private final String url;
    private final int index;
    private final String path;
    private final DownloadListener listener;
    private boolean isSingleDownload;
    private volatile boolean isPaused;
    private DownloadEntry.DownLoadStatus mStatus;
    private volatile boolean isCanceled;
    private volatile boolean isError;

    public DownloadThread(String url, int index, int startPos, int endPos, DownloadListener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        if (startPos == -1 && endPos == -1) {
            isSingleDownload = true;
        }
//        TODO: change the path
//        this.path = Environment.getExternalStorageState() + File.separator
//                + "henry" + File.separator + url.substring(url.lastIndexOf("/") + 1);
        this.path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "henry" + url.substring(url.lastIndexOf("/") + 1);
        Trace.e("DownloadThread: path: " + path);
        this.listener = listener;
    }

    @Override
    public void run() {
        mStatus = DownloadEntry.DownLoadStatus.downloading;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIME_OUT);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            File file = new File(path);
            RandomAccessFile raf;
            FileOutputStream fos;
            InputStream is = null;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCanceled || isError) break;
                    raf.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }
                raf.close();
                is.close();
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                fos = new FileOutputStream(file);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCanceled || isError) break;
                    fos.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }
                fos.close();
                is.close();
            }
            if (isPaused) {
                mStatus = DownloadEntry.DownLoadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                mStatus = DownloadEntry.DownLoadStatus.canceled;
                listener.onDownloadCanceled(index);
            } else if (isError) {
                mStatus = DownloadEntry.DownLoadStatus.error;
                listener.onDownloadError(index, "cancel manually by error");
            } else {
                mStatus = DownloadEntry.DownLoadStatus.completed;
                listener.onDownloadCompleted(index);
            }
//            listener.onConnected(isSupportRange, contentLength);
        } catch (Exception e) {
            Trace.e("DownloadThread: " + e.getMessage());
            if (isPaused) {
                mStatus = DownloadEntry.DownLoadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCanceled) {
                mStatus = DownloadEntry.DownLoadStatus.canceled;
                listener.onDownloadCanceled(index);
            } else {
                mStatus = DownloadEntry.DownLoadStatus.error;
                listener.onDownloadError(index, e.getMessage());
            }
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    public boolean isRunning() {
        return mStatus == DownloadEntry.DownLoadStatus.downloading;
    }

    public void pause() {
        isPaused = true;
        Thread.currentThread().interrupt();
    }

    public boolean isPaused() {
        return mStatus == DownloadEntry.DownLoadStatus.paused || mStatus == DownloadEntry.DownLoadStatus.completed;
    }

    public void cancel() {
        isCanceled = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCanceled() {
        return mStatus == DownloadEntry.DownLoadStatus.canceled || mStatus == DownloadEntry.DownLoadStatus.completed;
    }

    public boolean isError() {
        return mStatus == DownloadEntry.DownLoadStatus.error;
    }

    public void cancelByError() {
        isError = true;
        Thread.currentThread().interrupt();
    }

    interface DownloadListener {
        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String message);

        void onDownloadPaused(int index);

        void onDownloadCanceled(int index);
    }

}
