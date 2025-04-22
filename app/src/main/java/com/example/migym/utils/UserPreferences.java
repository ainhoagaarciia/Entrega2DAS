package com.example.migym.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;
import androidx.annotation.NonNull;

public class UserPreferences {
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_AGE = "age";
    private static final String KEY_HEART_PROBLEMS = "heart_problems";
    private static final String KEY_HEART_PROBLEMS_DETAILS = "heart_problems_details";
    private static final String KEY_PROFILE_IMAGE_URL = "profile_image_url";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_TIME_FORMAT = "time_format_24h";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_NAME = "name";

    private final SharedPreferences prefs;
    private final ImageStorage imageStorage;

    public UserPreferences(@NonNull Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        imageStorage = new ImageStorage(context);
    }

    public void saveProfileImageUrl(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            prefs.edit()
                .putString(KEY_PROFILE_IMAGE_URL, photoUrl)
                .apply();
            Log.d("UserPreferences", "Profile image URL saved: " + photoUrl);
        } else {
            Log.w("UserPreferences", "Attempted to save null or empty profile image URL");
        }
    }

    public String getProfileImageUrl() {
        String url = prefs.getString(KEY_PROFILE_IMAGE_URL, "");
        Log.d("UserPreferences", "Retrieved profile image URL: " + url);
        return url;
    }

    public void clearProfileImage() {
        prefs.edit()
            .remove(KEY_PROFILE_IMAGE_URL)
            .apply();
        Log.d("UserPreferences", "Profile image URL cleared");
    }

    public void saveUserData(String userId, String email, String username) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USERNAME, username)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply();
        Log.d("UserPreferences", "User data saved - ID: " + userId + ", Email: " + email + ", Username: " + username);
    }

    public void savePhone(String phone) {
        prefs.edit()
            .putString(KEY_PHONE, phone)
            .apply();
    }

    public void saveWeight(double weight) {
        prefs.edit()
            .putFloat(KEY_WEIGHT, (float) weight)
            .apply();
    }

    public void saveHeight(double height) {
        prefs.edit()
            .putFloat(KEY_HEIGHT, (float) height)
            .apply();
    }

    public void saveAge(int age) {
        prefs.edit()
            .putInt(KEY_AGE, age)
            .apply();
    }

    public void saveHeartProblems(boolean hasHeartProblems) {
        prefs.edit()
            .putBoolean(KEY_HEART_PROBLEMS, hasHeartProblems)
            .apply();
    }

    public void saveHeartProblemsDetails(String details) {
        prefs.edit()
            .putString(KEY_HEART_PROBLEMS_DETAILS, details)
            .apply();
    }

    public void saveProfileImagePath(String path) {
        prefs.edit()
            .putString(KEY_PROFILE_IMAGE_URL, path)
            .apply();
    }

    public String getProfileImagePath() {
        return prefs.getString(KEY_PROFILE_IMAGE_URL, null);
    }

    public void saveRole(String role) {
        prefs.edit()
            .putString(KEY_ROLE, role)
            .apply();
    }

    public void clearUserData() {
        clearProfileImage();
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PHONE)
            .remove(KEY_WEIGHT)
            .remove(KEY_HEIGHT)
            .remove(KEY_AGE)
            .remove(KEY_HEART_PROBLEMS)
            .remove(KEY_HEART_PROBLEMS_DETAILS)
            .remove(KEY_ROLE)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply();
        Log.d("UserPreferences", "All user data cleared");
    }

    public boolean isUserLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public double getWeight() {
        return prefs.getFloat(KEY_WEIGHT, 0);
    }

    public double getHeight() {
        return prefs.getFloat(KEY_HEIGHT, 0);
    }

    public int getAge() {
        return prefs.getInt(KEY_AGE, 0);
    }

    public boolean hasHeartProblems() {
        return prefs.getBoolean(KEY_HEART_PROBLEMS, false);
    }

    public String getHeartProblemsDetails() {
        return prefs.getString(KEY_HEART_PROBLEMS_DETAILS, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "user");
    }

    public void setLanguage(String language) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "es");
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setTimeFormat24h(boolean enabled) {
        prefs.edit().putBoolean(KEY_TIME_FORMAT, enabled).apply();
    }

    public boolean isTimeFormat24h() {
        return prefs.getBoolean(KEY_TIME_FORMAT, true);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply();
    }

    public void deleteProfileImage() {
        imageStorage.deleteProfileImage();
        prefs.edit().remove(KEY_PROFILE_IMAGE_URL).apply();
    }

    public void saveEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public void saveName(String name) {
        prefs.edit().putString(KEY_NAME, name).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
        imageStorage.deleteProfileImage();
    }
} 