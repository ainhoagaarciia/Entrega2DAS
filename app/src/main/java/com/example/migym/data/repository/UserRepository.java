package com.example.migym.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.migym.data.model.GoogleCloudStorageManager;
import com.example.migym.data.model.GoogleCloudStorageManager.UploadListener;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final GoogleCloudStorageManager storageManager;
    private UploadListener listener;

    public UserRepository(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.storageManager = new GoogleCloudStorageManager(context);
    }

    public void uploadImage(Uri imageUri, UploadListener callback) {
        this.listener = callback;
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }

        storageManager.uploadProfileImage(imageUri, userId, new UploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                updateUserProfileImage(imageUrl);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error uploading image: " + error);
                if (listener != null) {
                    listener.onError(error);
                }
            }

            @Override
            public void onProgress(double progress) {
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }
        });
    }

    private void updateUserProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.e(TAG, "Invalid image URL");
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "User not logged in");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUrl", imageUrl);
        updates.put("lastUpdated", System.currentTimeMillis());

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Profile image updated successfully");
                if (listener != null) {
                    listener.onSuccess(imageUrl);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating profile image", e);
                if (listener != null) {
                    listener.onError("Error updating profile: " + e.getMessage());
                }
            });
    }
} 