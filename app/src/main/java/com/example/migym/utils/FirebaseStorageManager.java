package com.example.migym.utils;

import android.net.Uri;
import android.util.Log;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;

public class FirebaseStorageManager {
    private static final String TAG = "FirebaseStorageManager";
    private final FirebaseStorage storage;
    private final StorageReference storageRef;

    public FirebaseStorageManager() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public void uploadImage(Uri imageUri, String folder, OnUploadListener listener) {
        try {
            // Crear una referencia única para la imagen
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageRef.child(folder).child(fileName);
            
            // Subir el archivo
            UploadTask uploadTask = imageRef.putFile(imageUri);
            
            // Manejar el progreso de la subida
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload progress: " + progress + "%");
            });
            
            // Manejar el éxito de la subida
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image uploaded successfully");
                // Obtener la URL de descarga
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    listener.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL: " + e.getMessage());
                    listener.onFailure(e);
                });
            });
            
            // Manejar el error de la subida
            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image: " + e.getMessage());
                listener.onFailure(e);
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in uploadImage: " + e.getMessage());
            listener.onFailure(e);
        }
    }

    public void getImageUrl(String path, OnImageUrlListener listener) {
        try {
            StorageReference imageRef = storageRef.child(path);
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        listener.onSuccess(uri.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting image URL: " + e.getMessage());
                        listener.onFailure(e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getImageUrl: " + e.getMessage());
            listener.onFailure(e);
        }
    }

    public interface OnUploadListener {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public interface OnImageUrlListener {
        void onSuccess(String url);
        void onFailure(Exception e);
    }
} 