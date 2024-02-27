package com.henry.downloader;

import java.util.Observable;
import java.util.Observer;

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry) arg);
        }
    }

    public abstract void notifyUpdate(DownloadEntry arg);
}
