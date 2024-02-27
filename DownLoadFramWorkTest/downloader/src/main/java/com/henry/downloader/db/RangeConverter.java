package com.henry.downloader.db;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.henry.downloader.DownloadEntry;

import java.lang.reflect.Type;
import java.util.HashMap;

public class RangeConverter {

    Gson gson = new Gson();

    @TypeConverter
    public String mapToString(HashMap<Integer, Integer> map) {
        return gson.toJson(map);
    }

    @TypeConverter
    public HashMap<Integer,Integer> stringToMap(String data) {
        if (data == null) {
            return null;
        }
        Type mapType = new TypeToken<HashMap<Integer, Integer>>() {}.getType();
        return gson.fromJson(data, mapType);
    }
}
