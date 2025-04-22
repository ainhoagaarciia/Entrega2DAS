package com.example.migym.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.StorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GoogleCloudStorageManager {
    private static final String TAG = "GoogleCloudStorageManager";
    private static final String BUCKET_NAME = "das25-456819.appspot.com";
    private static final int COMPRESSION_QUALITY = 85;
    private static final long TIMEOUT_SECONDS = 30;

    private final Context context;
    private final Storage storage;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
        void onProgress(double progress);
    }

    public interface DeleteListener {
        void onSuccess();
        void onError(String error);
    }

    public GoogleCloudStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        try {
            // Cargar credenciales desde el archivo JSON
            InputStream credentialsStream = context.getAssets().open("google-cloud-credentials.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            
            // Inicializar el cliente de Storage
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("das25-456819")
                    .build()
                    .getService();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing Google Cloud Storage", e);
            throw new RuntimeException("Error initializing Google Cloud Storage", e);
        }
    }

    public void uploadProfileImage(Uri imageUri, String userId, @NonNull UploadListener listener) {
        if (imageUri == null || listener == null) {
            listener.onError("Invalid parameters");
            return;
        }

        executorService.execute(() -> {
            try {
                // Validar y procesar la imagen
                Bitmap bitmap = getBitmapFromUri(imageUri);
                if (bitmap == null) {
                    notifyError(listener, "Error processing image");
                    return;
                }

                // Comprimir la imagen
                byte[] imageBytes = compressImage(bitmap);
                if (imageBytes == null) {
                    notifyError(listener, "Error compressing image");
                    return;
                }

                // Generar nombre único para la imagen
                String imageName = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                BlobId blobId = BlobId.of(BUCKET_NAME, imageName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType("image/jpeg")
                        .build();

                // Subir la imagen
                storage.create(blobInfo, imageBytes);

                // Construir la URL pública
                String imageUrl = String.format("https://storage.googleapis.com/%s/%s", 
                        BUCKET_NAME, imageName);

                // Notificar éxito
                notifySuccess(listener, imageUrl);

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                notifyError(listener, "Error uploading image: " + e.getMessage());
            }
        });
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Decodificar la imagen con opciones para reducir el tamaño
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reducir el tamaño a la mitad
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error reading image from URI", e);
            return null;
        }
    }

    private byte[] compressImage(Bitmap bitmap) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    private void notifySuccess(@NonNull UploadListener listener, String imageUrl) {
        mainHandler.post(() -> listener.onSuccess(imageUrl));
    }

    private void notifyError(@NonNull UploadListener listener, String error) {
        mainHandler.post(() -> listener.onError(error));
    }

    public void deleteImage(String imageUrl, @NonNull DeleteListener listener) {
        if (storage == null) {
            listener.onError("Storage no inicializado correctamente");
            return;
        }

        executorService.execute(() -> {
            try {
                // Extraer el nombre del archivo de la URL
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
                
                boolean deleted = storage.delete(blobId);
                if (deleted) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess());
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> 
                        listener.onError("No se pudo eliminar la imagen"));
                }
            } catch (StorageException e) {
                Log.e(TAG, "Error deleting from Cloud Storage", e);
                new Handler(Looper.getMainLooper()).post(() -> 
                    listener.onError("Error al eliminar la imagen: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Error deleting image", e);
                new Handler(Looper.getMainLooper()).post(() -> 
                    listener.onError("Error inesperado: " + e.getMessage()));
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 