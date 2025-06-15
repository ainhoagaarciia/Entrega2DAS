package com.example.migym.repositories;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.migym.data.AppDatabase;
import com.example.migym.data.WorkoutDao;
import com.example.migym.models.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {
    private final WorkoutDao workoutDao;
    private final LiveData<List<Workout>> allWorkouts;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Workout>> workoutsByDay = new MutableLiveData<>();
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface OnWorkoutOperationListener {
        void onSuccess(Workout workout);
        void onError(String error);
    }

    public interface OnWorkoutsLoadedListener {
        void onWorkoutsLoaded(List<Workout> workouts);
        void onError(String error);
    }

    public WorkoutRepository(Application application) {
        AppDatabase appDb = AppDatabase.getInstance(application);
        workoutDao = appDb.workoutDao();
        allWorkouts = workoutDao.getAllWorkouts();
        executorService = Executors.newSingleThreadExecutor();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Cargar entrenamientos de Firestore al iniciar
        loadWorkoutsFromFirestore();
    }

    private void loadWorkoutsFromFirestore() {
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
            .collection("workouts")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                executorService.execute(() -> {
                    // Limpiar la base de datos local
                    workoutDao.deleteAll();
                    
                    // Guardar los entrenamientos en la base de datos local
                    for (var doc : queryDocumentSnapshots) {
                        Workout workout = doc.toObject(Workout.class);
                        workout.setId(doc.getId());
                        workoutDao.insert(workout);
                    }
                    
                    // Actualizar el LiveData
                    List<Workout> workouts = workoutDao.getAllWorkoutsSync();
                    workoutsByDay.postValue(workouts);
                });
            })
            .addOnFailureListener(e -> {
                // Si falla la carga desde Firestore, mantener los datos locales
                Log.e("WorkoutRepository", "Error loading workouts from Firestore: " + e.getMessage());
            });
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
                
                // Guardar en Firestore primero
                if (auth.getCurrentUser() != null) {
                    String userId = auth.getCurrentUser().getUid();
                    db.collection("users").document(userId)
                        .collection("workouts")
                        .document(workout.getId())
                        .set(workout)
                        .addOnSuccessListener(aVoid -> {
                            // Después de guardar en Firestore, guardar en local
                            executorService.execute(() -> {
                                workoutDao.insert(workout);
                                
                                // Forzar actualización del LiveData
                                List<Workout> workouts = workoutDao.getAllWorkoutsSync();
                                workoutsByDay.postValue(workouts);
                                
                                if (listener != null) {
                                    listener.onSuccess(workout);
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            if (listener != null) {
                                listener.onError("Error saving workout to cloud: " + e.getMessage());
                            }
                        });
                } else {
                    // Si no hay usuario autenticado, solo guardar localmente
                    workoutDao.insert(workout);
                    if (listener != null) {
                        listener.onSuccess(workout);
                    }
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("Error inserting workout: " + e.getMessage());
                }
            }
        });
    }

    public void update(Workout workout) {
        executorService.execute(() -> {
            // Actualizar en Firestore primero
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                db.collection("users").document(userId)
                    .collection("workouts")
                    .document(workout.getId())
                    .set(workout)
                    .addOnSuccessListener(aVoid -> {
                        // Después de actualizar en Firestore, actualizar localmente
                        executorService.execute(() -> {
                            workoutDao.update(workout);
                            
                            // Forzar actualización del LiveData
                            List<Workout> workouts = workoutDao.getAllWorkoutsSync();
                            workoutsByDay.postValue(workouts);
                        });
                    });
            } else {
                // Si no hay usuario autenticado, solo actualizar localmente
                workoutDao.update(workout);
            }
        });
    }

    public void delete(Workout workout) {
        executorService.execute(() -> {
            // Eliminar de Firestore primero
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                db.collection("users").document(userId)
                    .collection("workouts")
                    .document(workout.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Después de eliminar de Firestore, eliminar localmente
                        executorService.execute(() -> {
                            workoutDao.delete(workout);
                            
                            // Forzar actualización del LiveData
                            List<Workout> workouts = workoutDao.getAllWorkoutsSync();
                            workoutsByDay.postValue(workouts);
                        });
                    });
            } else {
                // Si no hay usuario autenticado, solo eliminar localmente
                workoutDao.delete(workout);
            }
        });
    }

    public void deleteAll() {
        executorService.execute(() -> {
            // Eliminar de la base de datos local
            workoutDao.deleteAll();
            
            // Eliminar de Firestore
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                db.collection("users").document(userId)
                    .collection("workouts")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (var doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                    });
            }
        });
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
        loadWorkoutsFromFirestore();
    }
} 