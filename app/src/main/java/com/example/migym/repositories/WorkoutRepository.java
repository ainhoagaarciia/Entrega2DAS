package com.example.migym.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.migym.data.AppDatabase;
import com.example.migym.data.WorkoutDao;
import com.example.migym.models.Workout;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {
    private final WorkoutDao workoutDao;
    private final LiveData<List<Workout>> allWorkouts;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Workout>> workoutsByDay = new MutableLiveData<>();

    public interface OnWorkoutOperationListener {
        void onSuccess(Workout workout);
        void onError(String error);
    }

    public interface OnWorkoutsLoadedListener {
        void onWorkoutsLoaded(List<Workout> workouts);
        void onError(String error);
    }

    public WorkoutRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        workoutDao = db.workoutDao();
        allWorkouts = workoutDao.getAllWorkouts();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    public LiveData<Workout> getWorkoutById(String id) {
        return workoutDao.getWorkoutById(id);
    }

    public List<Workout> getWorkoutsAtTimeSync(int dayOfWeek, String time) {
        return workoutDao.getWorkoutsAtTimeSync(dayOfWeek, time);
    }

    public void insertWorkout(Workout workout, OnWorkoutOperationListener listener) {
        executorService.execute(() -> {
            try {
                if (workout.getId() == null || workout.getId().isEmpty()) {
                    workout.setId(UUID.randomUUID().toString());
                }
                workoutDao.insert(workout);
                
                // Forzar actualización del LiveData
                List<Workout> workouts = workoutDao.getAllWorkoutsSync();
                if (workoutsByDay.getValue() != null) {
                    workoutsByDay.postValue(workouts);
                }
                
                if (listener != null) {
                    listener.onSuccess(workout);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("Error inserting workout: " + e.getMessage());
                }
            }
        });
    }

    public void update(Workout workout) {
        executorService.execute(() -> workoutDao.update(workout));
    }

    public void delete(Workout workout) {
        executorService.execute(() -> workoutDao.delete(workout));
    }

    public void deleteAll() {
        executorService.execute(workoutDao::deleteAll);
    }

    public List<Workout> getWorkoutsByDaySync(int dayOfWeek) {
        return workoutDao.getWorkoutsByDaySync(dayOfWeek);
    }

    public LiveData<List<Workout>> getWorkoutsByDay(int dayOfWeek) {
        executorService.execute(() -> {
            List<Workout> workouts = workoutDao.getWorkoutsByDaySync(dayOfWeek);
            workoutsByDay.postValue(workouts);
        });
        return workoutsByDay;
    }

    public void loadWorkouts() {
        // Este método es opcional ya que LiveData se actualiza automáticamente
        // pero lo mantenemos por compatibilidad
        executorService.execute(() -> {
            List<Workout> workouts = workoutDao.getAllWorkoutsSync();
            // No necesitamos hacer nada aquí ya que LiveData se actualizará automáticamente
        });
    }
} 