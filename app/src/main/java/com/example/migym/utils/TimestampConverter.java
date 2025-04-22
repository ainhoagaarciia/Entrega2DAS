package com.example.migym.utils;

import androidx.room.TypeConverter;
import com.google.firebase.Timestamp;

public class TimestampConverter {
    @TypeConverter
    public static Timestamp fromLong(Long value) {
        return value == null ? null : new Timestamp(value / 1000, (int) ((value % 1000) * 1000000));
    }

    @TypeConverter
    public static Long toLong(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
    }
} 