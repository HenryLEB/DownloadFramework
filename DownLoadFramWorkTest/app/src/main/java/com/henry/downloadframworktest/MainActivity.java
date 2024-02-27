package com.henry.downloadframworktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.henry.downloader.DataWatcher;
import com.henry.downloader.DownloadEntry;
import com.henry.downloader.DownloadManager;
import com.henry.downloader.Trace;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mDownloadBtn;
    private Button mDownloadPauseBtn;
    private Button mDownloadCancelBtn;
    private DownloadManager mDownloadManager;
    private DownloadEntry entry;

    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry arg) {
            entry = arg;
            if (entry.status == DownloadEntry.DownLoadStatus.canceled)
                entry = null;
            Trace.e(arg.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDownloadBtn = findViewById(R.id.btn_download);
        mDownloadPauseBtn = findViewById(R.id.btn_download_pause);
        mDownloadCancelBtn = findViewById(R.id.btn_download_cancel);
        mDownloadBtn.setOnClickListener(this);
        mDownloadPauseBtn.setOnClickListener(this);
        mDownloadCancelBtn.setOnClickListener(this);
        mDownloadManager = DownloadManager.getInstance(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownloadManager.removeObserver(watcher);
    }

    @Override
    public void onClick(View v) {
        if (entry == null) {
            entry = new DownloadEntry();
            entry.name = "test.jpg";
            entry.url = "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png";
            entry.id = "1";
        }
        switch (v.getId()) {
            case R.id.btn_download:
                Trace.e(entry.toString());
                mDownloadManager.add(entry);
                break;
            case R.id.btn_download_pause:
                if (entry.status == DownloadEntry.DownLoadStatus.downloading)
                    mDownloadManager.pause(entry);
                else if (entry.status == DownloadEntry.DownLoadStatus.paused)
                    mDownloadManager.resume(entry);
            case R.id.btn_download_cancel:
                mDownloadManager.cancel(entry);
                break;
        }


    }
}