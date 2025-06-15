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
import android.graphics.Bitmap;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.SetOptions;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String BUCKET_NAME = "migym-app-bucket";
    private static final String PROJECT_ID = "migym-app";
    
    private final Context context;
    private final ExecutorService executorService;
    private final MutableLiveData<User> currentUser;
    private final Handler mainHandler;

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
        this.executorService = Executors.newSingleThreadExecutor();
        this.currentUser = new MutableLiveData<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User firebaseUserData = documentSnapshot.toObject(User.class);
                        if (firebaseUserData != null) {
                            firebaseUserData.setId(firebaseUser.getUid());
                            postToMainThread(() -> currentUser.setValue(firebaseUserData));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading from Firebase", e);
                });
        }
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void updateUserProfile(User user, OnProfileUpdateListener listener) {
        try {
            // Guardar en Firestore
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", user.getName());
                updates.put("email", user.getEmail());
                updates.put("weight", user.getWeight());
                updates.put("height", user.getHeight());
                updates.put("age", user.getAge());
                updates.put("heartProblems", user.hasHeartProblems());
                updates.put("heartProblemsDetails", user.getHeartProblemsDetails());
                updates.put("photoUrl", user.getPhotoUrl());
                updates.put("lastUpdated", new Date());

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(firebaseUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Perfil actualizado en Firestore");
                        postToMainThread(() -> currentUser.setValue(user));
                        listener.onSuccess(user.getPhotoUrl());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al actualizar perfil en Firestore: " + e.getMessage());
                        listener.onError("Error al actualizar perfil en la nube: " + e.getMessage());
                    });
            } else {
                listener.onError("Usuario no autenticado");
            }
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

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listener.onError("No hay usuario autenticado");
            return;
        }

        try {
            // Comprimir la imagen
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            // Subir a Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String fileName = "profile_" + firebaseUser.getUid() + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference photoRef = storageRef.child("profile_images/" + fileName);

            // Subir la imagen
            UploadTask uploadTask = photoRef.putBytes(data);
            
            // Escuchar el progreso
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                listener.onProgress(progress / 100.0);
            });

            // Escuchar el resultado
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return photoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        String downloadUrl = downloadUri.toString();
                        Log.d(TAG, "URL de descarga obtenida: " + downloadUrl);
                        
                        // Obtener el usuario actual de Firestore
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(firebaseUser.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                final User user;
                                if (documentSnapshot.exists()) {
                                    User tempUser = documentSnapshot.toObject(User.class);
                                    if (tempUser == null) {
                                        user = new User();
                                        user.setId(firebaseUser.getUid());
                                    } else {
                                        user = tempUser;
                                    }
                                } else {
                                    user = new User();
                                    user.setId(firebaseUser.getUid());
                                }
                                
                                // Actualizar el photoUrl
                                user.setPhotoUrl(downloadUrl);
                                
                                // Guardar en Firestore
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("photoUrl", downloadUrl);
                                updates.put("lastUpdated", new Date());
                                
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(updates, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "photoUrl actualizado en Firestore: " + downloadUrl);
                                        postToMainThread(() -> {
                                            currentUser.setValue(user);
                                            listener.onSuccess(downloadUrl);
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error en Firestore: " + e.getMessage());
                                        listener.onError("Error al guardar la imagen en la base de datos");
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al obtener usuario de Firestore: " + e.getMessage());
                                listener.onError("Error al obtener datos del usuario");
                            });
                    } else {
                        Log.e(TAG, "Error: downloadUri es null");
                        listener.onError("Error al obtener la URL de la imagen");
                    }
                } else {
                    Log.e(TAG, "Error al subir la imagen: " + task.getException());
                    listener.onError("Error al subir la imagen: " + task.getException().getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error general: " + e.getMessage());
            listener.onError("Error al procesar la imagen: " + e.getMessage());
        }
    }

    public void logout(OnLogoutListener listener) {
        try {
            FirebaseAuth.getInstance().signOut();
            listener.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error al hacer logout", e);
            listener.onError("Error al hacer logout: " + e.getMessage());
        }
    }

    private void postToMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public boolean isUserAuthenticated() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser != null;
    }

    public void saveUserProfileToFirebase(User user, OnProfileUpdateListener listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            if (listener != null) listener.onError("Usuario no autenticado");
            return;
        }
        String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess(user.getPhotoUrl());
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onError("Error al guardar perfil en la nube: " + e.getMessage());
            });
    }

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
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.setId(uid);
                        postToMainThread(() -> {
                            currentUser.setValue(user);
                            if (listener != null) listener.onUserLoaded(user);
                        });
                    } else {
                        if (listener != null) listener.onError("Error al cargar el perfil");
                    }
                } else {
                    // Si no existe, crearlo con los datos de FirebaseAuth
                    User newUser = new User();
                    newUser.setId(uid);
                    newUser.setName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
                    newUser.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
                    if (firebaseUser.getPhotoUrl() != null) {
                        newUser.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
                    }
                    db.collection("users").document(uid)
                        .set(newUser)
                        .addOnSuccessListener(aVoid -> {
                            postToMainThread(() -> {
                                currentUser.setValue(newUser);
                                if (listener != null) listener.onUserLoaded(newUser);
                            });
                        })
                        .addOnFailureListener(e -> {
                            if (listener != null) listener.onError("Error al crear el perfil: " + e.getMessage());
                        });
                }
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onError("Error al cargar perfil de la nube: " + e.getMessage());
            });
    }
} 