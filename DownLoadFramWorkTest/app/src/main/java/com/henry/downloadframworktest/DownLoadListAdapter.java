package com.henry.downloadframworktest;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.henry.downloader.DownloadEntry;
import com.henry.downloader.DownloadManager;

import java.util.List;

public class DownLoadListAdapter extends RecyclerView.Adapter<DownLoadListAdapter.VH> {
    List<DownloadEntry> entryList;



    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false);
        return new VH(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DownloadEntry entry = entryList.get(position);
        if (entry != null) {
            holder.tv_id.setText(entry.id);
            holder.tv_name.setText(entry.name);
            holder.tv_status.setText(entry.status + " ");
            holder.tv_current_length.setText(entry.currentLength + " ");
            holder.btn_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (entry.status) {
                        case idle:
                        case canceled:
                            DownloadManager.getInstance(v.getContext()).add(entry);
                            break;
                        case downloading:
                        case waiting:
                            DownloadManager.getInstance(v.getContext()).pause(entry);
//                            DownloadManager.getInstance(v.getContext()).cancel(entry);
                            break;
                        case paused:
                            DownloadManager.getInstance(v.getContext()).resume(entry);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (entryList != null)
            return entryList.size();
        return 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        public TextView tv_id;
        public TextView tv_name;
        public TextView tv_status;
        public TextView tv_current_length;
        public Button btn_download;

        public VH(@NonNull View itemView) {
            super(itemView);
            tv_id = itemView.findViewById(R.id.tv_entry_id);
            tv_name = itemView.findViewById(R.id.tv_entry_name);
            tv_status = itemView.findViewById(R.id.tv_entry_status);
            tv_current_length = itemView.findViewById(R.id.tv_entry_current_length);
            btn_download = itemView.findViewById(R.id.btn_download);
        }
    }

    public List<DownloadEntry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<DownloadEntry> entryList) {
        this.entryList = entryList;
        notifyDataSetChanged();
    }
}
