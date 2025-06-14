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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

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

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
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
            // Subir la imagen a Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            StorageReference photoRef = storageRef.child("profile_images/" + fileName);

            UploadTask uploadTask = photoRef.putFile(photoUri);
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                listener.onProgress(progress / 100.0);
            });
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    // Guardar la URL en preferencias
                    userPreferences.saveProfileImagePath(downloadUrl);
                    // Actualizar el usuario actual con la nueva imagen
                    User currentUserValue = currentUser.getValue();
                    if (currentUserValue != null) {
                        currentUserValue.setPhotoUrl(downloadUrl);
                        currentUser.postValue(currentUserValue);
                    }
                    listener.onSuccess(downloadUrl);
                }).addOnFailureListener(e -> {
                    listener.onError("No se pudo obtener la URL de descarga de la imagen");
                });
            });
            uploadTask.addOnFailureListener(e -> {
                listener.onError("Error al subir la imagen: " + e.getMessage());
            });
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al subir la imagen", e);
            listener.onError("Error inesperado al subir la imagen");
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

    // Guardar perfil en Firestore
    public void saveUserProfileToFirebase(User user, OnProfileUpdateListener listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            if (listener != null) listener.onError("Usuario no autenticado");
            return;
        }
        String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess(user.getPhotoUrl());
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onError("Error al guardar perfil en la nube: " + e.getMessage());
            });
    }

    // Cargar perfil desde Firestore
    public void loadUserProfileFromFirebase(OnUserLoadedListener listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            if (listener != null) listener.onError("Usuario no autenticado");
            return;
        }
        String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    if (listener != null) listener.onUserLoaded(user);
                } else {
                    if (listener != null) listener.onError("Perfil no encontrado en la nube");
                }
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onError("Error al cargar perfil de la nube: " + e.getMessage());
            });
    }
} 