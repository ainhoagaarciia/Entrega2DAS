package com.example.migym.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.example.migym.data.converters.UserPreferencesConverter;
import java.io.Serializable;

@Entity(tableName = "users")
@TypeConverters(UserPreferencesConverter.class)
public class User implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String email;
    private String phone;
    private String photoUrl;
    private double weight;
    private double height;
    private int age;
    @PropertyName("heartProblems")
    private boolean heartProblems;
    private String heartProblemsDetails;
    private String role;
    private String fcmToken;
    private Timestamp lastLogin;
    private boolean isActive;
    private UserPreferences preferences;
    private String userId;
    private int gender;

    public User() {
        this.id = "current_user"; // Solo tendremos un usuario
        this.isActive = true;
        this.preferences = new UserPreferences();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @PropertyName("heartProblems")
    public boolean hasHeartProblems() {
        return heartProblems;
    }

    @PropertyName("heartProblems")
    public void setHeartProblems(boolean heartProblems) {
        this.heartProblems = heartProblems;
    }

    public String getHeartProblemsDetails() {
        return heartProblemsDetails;
    }

    public void setHeartProblemsDetails(String heartProblemsDetails) {
        this.heartProblemsDetails = heartProblemsDetails;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                ", age=" + age +
                ", heartProblems=" + heartProblems +
                ", heartProblemsDetails='" + heartProblemsDetails + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", lastUpdated=" + lastLogin +
                '}';
    }

    public static class UserPreferences implements Serializable {
        private String language;
        private boolean notificationsEnabled;
        private boolean darkMode;
        private boolean timeFormat24h;

        public UserPreferences() {
            this.language = "es";
            this.notificationsEnabled = true;
            this.darkMode = false;
            this.timeFormat24h = true;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public boolean isNotificationsEnabled() {
            return notificationsEnabled;
        }

        public void setNotificationsEnabled(boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }

        public boolean isDarkMode() {
            return darkMode;
        }

        public void setDarkMode(boolean darkMode) {
            this.darkMode = darkMode;
        }

        public boolean isTimeFormat24h() {
            return timeFormat24h;
        }

        public void setTimeFormat24h(boolean timeFormat24h) {
            this.timeFormat24h = timeFormat24h;
        }
    }
} 