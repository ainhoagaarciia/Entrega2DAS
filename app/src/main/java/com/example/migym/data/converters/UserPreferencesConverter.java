package com.example.migym.data.converters;

import androidx.room.TypeConverter;
import com.example.migym.models.User;
import com.google.gson.Gson;

public class UserPreferencesConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromUserPreferences(User.UserPreferences userPreferences) {
        return userPreferences == null ? null : gson.toJson(userPreferences);
    }

    @TypeConverter
    public static User.UserPreferences toUserPreferences(String userPreferencesString) {
        return userPreferencesString == null ? null : gson.fromJson(userPreferencesString, User.UserPreferences.class);
    }
} 