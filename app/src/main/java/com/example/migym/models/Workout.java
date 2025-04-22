package com.example.migym.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.Objects;
import java.util.Date;

/**
 * Entity class representing a workout in the application.
 * This class is used with Room database to store workout information.
 */
@Entity(tableName = "workouts")
public class Workout {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id = "";

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "title")
    private String title = "";

    @ColumnInfo(name = "description")
    private String description = "";

    @ColumnInfo(name = "type")
    @PropertyName("type")
    private String type;

    @ColumnInfo(name = "day_of_week")
    private int dayOfWeek;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "instructor")
    private String instructor = "";

    @ColumnInfo(name = "location")
    private String location = "";

    @ColumnInfo(name = "completed")
    private int completed;

    @ColumnInfo(name = "difficulty")
    private String difficulty;

    @ColumnInfo(name = "equipment")
    private String equipment;

    @ColumnInfo(name = "muscle_groups")
    private String muscleGroups;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "distance")
    private double distance;

    @ColumnInfo(name = "speed")
    private double speed;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "performance")
    private int performance;

    @ColumnInfo(name = "notification_time")
    private int notificationTime; // 0: none, 15: 15 minutes, 30: 30 minutes, 60: 1 hour

    @ColumnInfo(name = "notification_enabled")
    private boolean notificationEnabled;

    /**
     * Default constructor required by Room
     */
    public Workout() {
        // Constructor vacío requerido por Room y Firebase
    }

    /**
     * Constructor for creating a basic workout
     * @param name Workout name
     * @param type Type of workout
     * @param dayOfWeek Day of the week (1-7, where 1 is Sunday)
     * @param time Time of the workout in HH:mm format
     */
    @Ignore
    public Workout(String name, String typeStr, int dayOfWeek, String time) {
        this.name = name;
        this.type = typeStr;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.title = name;
        this.description = "";
        this.duration = 0;
        this.instructor = "";
        this.location = "";
        this.completed = 0;
    }

    @Exclude
    @NonNull
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.title = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.name = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }

    public int getTypeAsInt() {
        try {
            return Integer.parseInt(type);
        } catch (NumberFormatException e) {
            return 0; // Default to "Other"
        }
    }

    public void setTypeFromInt(int type) {
        this.type = String.valueOf(type);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor != null ? instructor : "";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location != null ? location : "";
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(String muscleGroups) {
        this.muscleGroups = muscleGroups;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getPerformance() {
        return performance;
    }

    public void setPerformance(int performance) {
        this.performance = performance;
    }

    public int getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(int notificationTime) {
        this.notificationTime = notificationTime;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workout workout = (Workout) o;
        return dayOfWeek == workout.dayOfWeek &&
               duration == workout.duration &&
               completed == workout.completed &&
               Objects.equals(id, workout.id) &&
               Objects.equals(name, workout.name) &&
               Objects.equals(title, workout.title) &&
               Objects.equals(description, workout.description) &&
               Objects.equals(type, workout.type) &&
               Objects.equals(time, workout.time) &&
               Objects.equals(instructor, workout.instructor) &&
               Objects.equals(location, workout.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, title, description, type, dayOfWeek, time, duration, instructor, location, completed);
    }

    /**
     * Validates if the workout time conflicts with another workout
     * @param other The other workout to check against
     * @return true if there is a time conflict
     */
    public boolean hasTimeConflict(Workout other) {
        return this.dayOfWeek == other.dayOfWeek && 
               this.time.equals(other.time) &&
               !this.id.equals(other.id);
    }

    /**
     * Gets a formatted string representation of the workout type
     * @return The workout type in a user-friendly format
     */
    public String getFormattedType() {
        String[] types = {"Other", "Cardio", "Strength", "Flexibility", "HIIT"};
        try {
            int typeIndex = Integer.parseInt(type);
            return typeIndex >= 0 && typeIndex < types.length ? types[typeIndex] : "Other";
        } catch (NumberFormatException e) {
            return type != null ? type : "Other";
        }
    }

    /**
     * Gets the day of week as a localized string
     * @return The name of the day (e.g., "Monday", "Tuesday", etc.)
     */
    public String getDayOfWeekString() {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        int index = (dayOfWeek - 1) % 7;
        return days[index < 0 ? 0 : index];
    }
} 