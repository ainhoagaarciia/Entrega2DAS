package com.example.migym.data.model;

public interface UploadListener {
    void onSuccess(String imageUrl);
    void onError(String error);
    void onProgress(double progress);
} 