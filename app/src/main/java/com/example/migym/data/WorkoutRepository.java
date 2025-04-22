package com.example.migym.data;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.migym.models.Workout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {
    private static final String TAG = "WorkoutRepository";
    private final WorkoutDao workoutDao;
    private final LiveData<List<Workout>> allWorkouts;
    private final ExecutorService executorService;

    public interface OnWorkoutsLoadedListener {
        void onWorkoutsLoaded(List<Workout> workouts);
        void onError(String error);
    }

    public interface OnWorkoutOperationListener {
        void onSuccess(Workout workout);
        void onError(String error);
    }

    public WorkoutRepository(@NonNull Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        workoutDao = db.workoutDao();
        allWorkouts = workoutDao.getAllWorkouts();
        executorService = Executors.newSingleThreadExecutor();
    }

    @NonNull
    public LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    public LiveData<Workout> getWorkoutById(String id) {
        return workoutDao.getWorkoutById(id);
    }

    public void insertWorkout(Workout workout, OnWorkoutOperationListener listener) {
        if (workout == null) {
            Log.e(TAG, "Error: workout is null");
            if (listener != null) {
                listener.onError("Workout cannot be null");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                // Check for time conflicts
                List<Workout> conflicts = workoutDao.getWorkoutsAtTimeSync(workout.getDayOfWeek(), workout.getTime());
                if (!conflicts.isEmpty()) {
                    Log.w(TAG, "Time conflict detected");
                    if (listener != null) {
                        listener.onError("There is already a workout scheduled at this time");
                    }
                    return;
                }

                // Insert the workout
                long id = workoutDao.insert(workout);
                workout.setId(String.valueOf(id));
                
                if (listener != null) {
                    listener.onSuccess(workout);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting workout", e);
                if (listener != null) {
                    listener.onError("Error inserting workout: " + e.getMessage());
                }
            }
        });
    }

    public void update(Workout workout) {
        executorService.execute(() -> {
            try {
                workoutDao.update(workout);
            } catch (Exception e) {
                Log.e(TAG, "Error updating workout", e);
            }
        });
    }

    public void delete(Workout workout) {
        executorService.execute(() -> {
            try {
                workoutDao.delete(workout);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting workout", e);
            }
        });
    }

    public void deleteAll() {
        executorService.execute(() -> {
            try {
                workoutDao.deleteAll();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all workouts", e);
            }
        });
    }

    public List<Workout> getWorkoutsAtTimeSync(int dayOfWeek, String time) {
        return workoutDao.getWorkoutsAtTimeSync(dayOfWeek, time);
    }

    public LiveData<List<Workout>> getWorkoutsByDay(int dayOfWeek) {
        return new MutableLiveData<>(workoutDao.getWorkoutsByDaySync(dayOfWeek));
    }

    public void cleanup() {
        executorService.shutdown();
    }
} 