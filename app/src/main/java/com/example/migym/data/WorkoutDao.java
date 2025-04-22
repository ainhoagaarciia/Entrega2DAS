package com.example.migym.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.migym.models.Workout;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY day_of_week ASC, time ASC")
    LiveData<List<Workout>> getAllWorkouts();

    @Query("SELECT * FROM workouts ORDER BY day_of_week ASC, time ASC")
    List<Workout> getAllWorkoutsSync();

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    LiveData<Workout> getWorkoutById(String workoutId);

    @Query("SELECT * FROM workouts WHERE day_of_week = :dayOfWeek")
    List<Workout> getWorkoutsByDaySync(int dayOfWeek);

    @Query("SELECT * FROM workouts WHERE day_of_week = :dayOfWeek AND time = :time")
    List<Workout> getWorkoutsAtTimeSync(int dayOfWeek, String time);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Workout workout);

    @Update
    void update(Workout workout);

    @Delete
    void delete(Workout workout);

    @Query("DELETE FROM workouts")
    void deleteAll();
} 