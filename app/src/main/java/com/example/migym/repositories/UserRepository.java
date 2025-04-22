package com.example.migym.repositories;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.migym.models.User;
import com.example.migym.data.AppDatabase;
import com.example.migym.data.UserDao;
import com.example.migym.utils.LocalImageStorage;
import com.example.migym.utils.UserPreferences;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.firebase.auth.FirebaseAuth;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String BUCKET_NAME = "migym-app-bucket";
    private static final String PROJECT_ID = "migym-app";
    
    private final Context context;
    private final UserDao userDao;
    private final ExecutorService executorService;
    private final LocalImageStorage imageStorage;
    private final MutableLiveData<User> currentUser;
    private final Handler mainHandler;
    private final UserPreferences userPreferences;

    public interface OnProfileUpdateListener {
        void onSuccess(String imageUrl);
        void onError(String error);
        void onProgress(double progress);
    }

    public interface OnLogoutListener {
        void onSuccess();
        void onError(String error);
    }

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.imageStorage = new LocalImageStorage(context);
        this.currentUser = new MutableLiveData<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.userPreferences = new UserPreferences(context);
        
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        executorService.execute(() -> {
            final User user = userDao.getUserSync();
            if (user == null) {
                final User newUser = new User();
                userDao.insert(newUser);
                postToMainThread(() -> currentUser.setValue(newUser));
            } else {
                postToMainThread(() -> currentUser.setValue(user));
            }
        });
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void updateUserProfile(User user, OnProfileUpdateListener listener) {
        try {
            // Guardar los datos del usuario en las preferencias
            userPreferences.saveName(user.getName());
            userPreferences.saveEmail(user.getEmail());
            userPreferences.saveWeight(user.getWeight());
            userPreferences.saveHeight(user.getHeight());
            userPreferences.saveAge(user.getAge());
            userPreferences.saveHeartProblems(user.hasHeartProblems());
            userPreferences.saveHeartProblemsDetails(user.getHeartProblemsDetails());
            
            // Si hay una nueva imagen, guardarla
            if (user.getPhotoUrl() != null) {
                Uri imageUri = Uri.parse(user.getPhotoUrl());
                String imagePath = imageStorage.saveImage(imageUri);
                if (imagePath != null) {
                    userPreferences.saveProfileImagePath(imagePath);
                }
            }
            
            listener.onSuccess(user.getPhotoUrl());
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar el perfil", e);
            listener.onError("Error al actualizar el perfil: " + e.getMessage());
        }
    }

    public void uploadProfileImage(Uri photoUri, OnProfileUpdateListener listener) {
        if (photoUri == null) {
            listener.onError("No se ha seleccionado ninguna imagen");
            return;
        }

        try {
            // Verificar si hay una imagen anterior y eliminarla
            String oldImagePath = userPreferences.getProfileImageUrl();
            if (oldImagePath != null && !oldImagePath.isEmpty()) {
                imageStorage.deleteImage(oldImagePath);
            }

            // Verificar el tipo de archivo
            String mimeType = context.getContentResolver().getType(photoUri);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                listener.onError("El archivo seleccionado no es una imagen válida");
                return;
            }

            // Guardar la nueva imagen
            String imagePath = imageStorage.saveImage(photoUri);
            if (imagePath == null || imagePath.isEmpty()) {
                listener.onError("No se pudo guardar la imagen");
                return;
            }

            Log.d(TAG, "Imagen guardada en: " + imagePath);
            
            // Actualizar la URL de la imagen en las preferencias
            userPreferences.saveProfileImagePath(imagePath);
            
            // Actualizar el usuario actual con la nueva imagen
            User currentUserValue = currentUser.getValue();
            if (currentUserValue != null) {
                currentUserValue.setPhotoUrl(imagePath);
                currentUser.postValue(currentUserValue);
            }
            
            // Notificar éxito
            listener.onSuccess(imagePath);
            
        } catch (SecurityException e) {
            Log.e(TAG, "Error de permisos al acceder a la imagen", e);
            listener.onError("No hay permisos suficientes para acceder a la imagen");
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al procesar la imagen", e);
            listener.onError("Error inesperado al procesar la imagen");
        }
    }

    public void logout(OnLogoutListener listener) {
        try {
            // Limpiar las preferencias
            userPreferences.clearUserData();
            
            // Eliminar la imagen de perfil si existe
            String currentImagePath = userPreferences.getProfileImageUrl();
            if (currentImagePath != null) {
                imageStorage.deleteImage(currentImagePath);
            }
            
            listener.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error al hacer logout", e);
            listener.onError("Error al hacer logout: " + e.getMessage());
        }
    }

    private void postToMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public LocalImageStorage getImageStorage() {
        return imageStorage;
    }

    public void cleanup() {
        executorService.shutdown();
    }

    public boolean isUserAuthenticated() {
        return userPreferences.isLoggedIn();
    }
} 