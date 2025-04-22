package com.example.migym.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.migym.models.Workout;
import com.example.migym.repositories.WorkoutRepository;
import com.example.migym.notifications.WorkoutNotificationManager;

public class WorkoutViewModel extends AndroidViewModel {
    private static final String TAG = "WorkoutViewModel";
    private final WorkoutRepository repository;
    private final LiveData<List<Workout>> allWorkouts;
    private final ExecutorService executorService;
    private final WorkoutNotificationManager notificationManager;
    private final MutableLiveData<Workout> selectedWorkout = new MutableLiveData<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnWorkoutAddListener {
        void onWorkoutAdded();
        void onError(String error);
        void onConflict(List<Workout> conflicts);
    }

    public WorkoutViewModel(Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allWorkouts = repository.getAllWorkouts();
        executorService = Executors.newSingleThreadExecutor();
        notificationManager = new WorkoutNotificationManager(application);
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    public void addWorkout(Workout workout, OnWorkoutAddListener listener) {
        if (workout == null) {
            Log.e(TAG, "addWorkout: workout is null");
            if (listener != null) {
                listener.onError("Workout cannot be null");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                // Validate workout data
                if (!validateWorkout(workout, listener)) {
                    return;
                }

                // Check for time conflicts
                List<Workout> conflicts = repository.getWorkoutsAtTimeSync(workout.getDayOfWeek(), workout.getTime());
                if (!conflicts.isEmpty()) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onConflict(conflicts);
                        }
                    });
                    return;
                }

                // Add workout to repository
                repository.insertWorkout(workout, new WorkoutRepository.OnWorkoutOperationListener() {
                    @Override
                    public void onSuccess(Workout addedWorkout) {
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onWorkoutAdded();
                            }
                            // Schedule notification if enabled
                            if (addedWorkout.isNotificationEnabled()) {
                                notificationManager.scheduleWorkoutNotification(addedWorkout);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onError(error);
                            }
                        });
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error adding workout", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError("Error adding workout: " + e.getMessage());
                    }
                });
            }
        });
    }

    private boolean validateWorkout(Workout workout, OnWorkoutAddListener listener) {
        if (workout.getTitle() == null || workout.getTitle().trim().isEmpty()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onError("Title is required");
                }
            });
            return false;
        }

        if (workout.getTime() == null || workout.getTime().trim().isEmpty()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onError("Time is required");
                }
            });
            return false;
        }

        if (workout.getDayOfWeek() < 0 || workout.getDayOfWeek() > 6) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onError("Invalid day of week");
                }
            });
            return false;
        }

        if (workout.getDuration() <= 0) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onError("Duration must be positive");
                }
            });
            return false;
        }

        if (workout.getLocation() == null || workout.getLocation().trim().isEmpty()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onError("Location is required");
                }
            });
            return false;
        }

        return true;
    }

    public void updateWorkout(Workout workout) {
        if (workout == null) {
            Log.e(TAG, "updateWorkout: workout is null");
            return;
        }

        executorService.execute(() -> {
            try {
                // Check for time conflicts with other workouts (excluding this one)
                List<Workout> conflicts = repository.getWorkoutsAtTimeSync(workout.getDayOfWeek(), workout.getTime());
                boolean hasConflict = false;
                for (Workout existing : conflicts) {
                    if (!existing.getId().equals(workout.getId())) {
                        hasConflict = true;
                        break;
                    }
                }

                if (!hasConflict) {
                    repository.update(workout);
                    notificationManager.scheduleWorkoutNotification(workout);
                } else {
                    Log.e(TAG, "Time conflict when updating workout");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating workout", e);
            }
        });
    }

    public void deleteWorkout(Workout workout) {
        if (workout == null) {
            Log.e(TAG, "deleteWorkout: workout is null");
            return;
        }

        executorService.execute(() -> {
            try {
                repository.delete(workout);
                notificationManager.cancelWorkoutNotification(workout.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting workout", e);
            }
        });
    }

    public LiveData<Workout> getWorkoutById(String id) {
        return repository.getWorkoutById(id);
    }

    public LiveData<List<Workout>> getWorkoutsByDay(int dayOfWeek) {
        return repository.getWorkoutsByDay(dayOfWeek);
    }

    private boolean hasTimeConflict(Workout workout) {
        if (workout == null) return false;
        
        List<Workout> conflicts = repository.getWorkoutsAtTimeSync(workout.getDayOfWeek(), workout.getTime());
        return conflicts != null && !conflicts.isEmpty();
    }

    public void deleteAllWorkouts() {
        executorService.execute(() -> {
            try {
                repository.deleteAll();
                notificationManager.cancelAllNotifications();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all workouts", e);
            }
        });
    }

    public void setSelectedWorkout(Workout workout) {
        selectedWorkout.setValue(workout);
    }

    public LiveData<Workout> getSelectedWorkout() {
        return selectedWorkout;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 