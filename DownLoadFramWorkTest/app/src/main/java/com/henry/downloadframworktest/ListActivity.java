package com.henry.downloadframworktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.henry.downloader.DataWatcher;
import com.henry.downloader.DownloadEntry;
import com.henry.downloader.DownloadManager;
import com.henry.downloader.Trace;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private DownloadManager mDownloadManager;
    private List<DownloadEntry> entryList = new ArrayList<>();
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry arg) {
            int index = entryList.indexOf(arg);
            if (index != -1) {
                Trace.e("notifyUpdate");
                entryList.remove(index);
                entryList.add(index, arg);
//                downLoadListAdapter.setEntryList(entryList);
                downLoadListAdapter.notifyDataSetChanged();
            }
            Trace.e(arg.toString());
        }
    };
    private RecyclerView recyclerView;
    private DownLoadListAdapter downLoadListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDownloadManager = DownloadManager.getInstance(this);
        initData();
    }

    private void initData() {
        entryList.add(new DownloadEntry("1", "test.name1", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("2", "test.name2", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("3", "test.name3", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("4", "test.name4", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("5", "test.name5", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("6", "test.name6", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("7", "test.name7", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("8", "test.name8", "https://cdn.gowan8.com/upload/image/202110/icon_202110131537406572.png"));
        entryList.add(new DownloadEntry("9", "test.name9", "https://yxfile.gowan8.com/putin/adpackage/307/307_123_1.0.1.apk"));
        entryList.add(new DownloadEntry("10", "test.name10", "https://yxfile.gowan8.com/putin/adpackage/307/307_123_1.0.1.apk"));

        DownloadEntry entry = null;
        DownloadEntry realEntry = null;
        for (int i = 0; i < entryList.size(); i++) {
            entry = entryList.get(i);
            realEntry = mDownloadManager.queryDownloadEntry(entry.id);
            if (realEntry != null) {
                entryList.remove(i);
                entryList.add(i, realEntry);
            }
        }

        recyclerView = findViewById(R.id.rv_download);
        downLoadListAdapter = new DownLoadListAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(downLoadListAdapter);
        downLoadListAdapter.setEntryList(entryList);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action:
                if (item.getTitle().equals("PAUSE ALL")) {
                    item.setTitle("RECOVER ALL");
                    mDownloadManager.pauseAll();
                } else {
                    item.setTitle("PAUSE ALL");
                    mDownloadManager.recoverAll();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }
}