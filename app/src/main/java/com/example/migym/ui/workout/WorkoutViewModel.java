package com.example.migym.ui.workout;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import com.example.migym.data.WorkoutRepository;
import com.example.migym.models.Workout;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;

public class WorkoutViewModel extends AndroidViewModel {
    private static final String TAG = "WorkoutViewModel";
    private final WorkoutRepository repository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface OnWorkoutAddListener {
        void onWorkoutAdded();
        void onError(String error);
        void onConflict(List<Workout> conflicts);
    }

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return repository.getAllWorkouts();
    }

    public LiveData<List<Workout>> getWorkoutsByDay(String day) {
        return repository.getWorkoutsByDay(Integer.parseInt(day));
    }

    public LiveData<Workout> getWorkoutById(String id) {
        return repository.getWorkoutById(id);
    }

    public void deleteWorkout(Workout workout) {
        repository.delete(workout);
    }

    public void addWorkout(Workout workout, OnWorkoutAddListener listener) {
        if (workout == null) {
            if (listener != null) {
                listener.onError("Error al añadir el entrenamiento: el entrenamiento no puede ser nulo");
            }
            return;
        }

        try {
            executorService.execute(() -> {
                try {
                    // Verificar conflictos de horario
                    List<Workout> conflicts = repository.getWorkoutsAtTimeSync(workout.getDayOfWeek(), workout.getTime());
                    if (!conflicts.isEmpty()) {
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onConflict(conflicts);
                            }
                        });
                        return;
                    }

                    // Añadir el workout
                    repository.insertWorkout(workout, new WorkoutRepository.OnWorkoutOperationListener() {
                        @Override
                        public void onSuccess(Workout workout) {
                            mainHandler.post(() -> {
                                if (listener != null) {
                                    listener.onWorkoutAdded();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            mainHandler.post(() -> {
                                if (listener != null) {
                                    listener.onError("Error al añadir el entrenamiento: " + error);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error adding workout", e);
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onError("Error al añadir el entrenamiento: " + e.getMessage());
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in addWorkout", e);
            if (listener != null) {
                listener.onError("Error al procesar la solicitud: " + e.getMessage());
            }
        }
    }

    public void forceAddWorkout(Workout workout) {
        if (workout == null) {
            Log.e(TAG, "Error: workout is null in forceAddWorkout");
            return;
        }

        repository.insertWorkout(workout, new WorkoutRepository.OnWorkoutOperationListener() {
            @Override
            public void onSuccess(Workout workout) {
                Log.d(TAG, "Workout forced added successfully");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error forcing workout add: " + error);
            }
        });
    }

    public void updateWorkout(Workout workout) {
        repository.update(workout);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public LiveData<List<Workout>> getWorkouts() {
        return repository.getAllWorkouts();
    }
} 