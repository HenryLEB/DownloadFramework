package com.henry.downloader;

import android.util.Log;

public class Trace {
    public static final String TAG = "TraceLog";
    private static final boolean DEBUG = true;

    public static void d(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }


}
