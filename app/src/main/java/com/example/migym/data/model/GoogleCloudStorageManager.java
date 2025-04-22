package com.example.migym.data.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GoogleCloudStorageManager {
    private static final String TAG = "GoogleCloudStorageManager";
    private static final String BUCKET_NAME = "migym-profile-images"; // Reemplazar con tu bucket real
    private static final int MAX_IMAGE_SIZE = 1024 * 1024; // 1MB
    private static final int COMPRESSION_QUALITY = 80;

    private final Context context;
    private final Storage storage;
    private final ExecutorService executorService;

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
        void onProgress(double progress);
    }

    public GoogleCloudStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void uploadProfileImage(Uri imageUri, String userId, @NonNull UploadListener listener) {
        if (imageUri == null || userId == null) {
            listener.onError("Invalid parameters");
            return;
        }

        executorService.execute(() -> {
            try {
                // Obtener y comprimir la imagen
                byte[] imageData = getCompressedImageData(imageUri);
                if (imageData == null) {
                    listener.onError("Error processing image");
                    return;
                }

                // Generar nombre único para la imagen
                String imageName = "profile_" + userId + "_" + UUID.randomUUID() + ".jpg";
                String path = "profile_images/" + imageName;

                // Crear el blob en Google Cloud Storage
                BlobId blobId = BlobId.of(BUCKET_NAME, path);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType("image/jpeg")
                        .build();

                // Subir la imagen
                Blob blob = storage.create(blobInfo, imageData);
                
                if (blob == null) {
                    listener.onError("Failed to upload image");
                    return;
                }

                // Obtener la URL pública de la imagen
                String imageUrl = String.format("https://storage.googleapis.com/%s/%s", 
                        BUCKET_NAME, path);

                listener.onSuccess(imageUrl);

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                listener.onError("Error uploading image: " + e.getMessage());
            }
        });
    }

    private byte[] getCompressedImageData(Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            return null;
        }

        // Decodificar la imagen
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        if (originalBitmap == null) {
            return null;
        }

        // Comprimir la imagen
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);
        
        byte[] compressedData = outputStream.toByteArray();
        originalBitmap.recycle();
        
        return compressedData;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        executorService.execute(() -> {
            try {
                // Extraer el nombre del archivo de la URL
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                String path = "profile_images/" + fileName;
                
                // Eliminar el blob
                BlobId blobId = BlobId.of(BUCKET_NAME, path);
                storage.delete(blobId);
                
                Log.d(TAG, "Image deleted successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting image", e);
            }
        });
    }

    public void downloadImage(String imageUrl, String fileName, OnSuccessListener<File> successListener, OnFailureListener failureListener) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            failureListener.onFailure(new Exception("Invalid image URL"));
            return;
        }

        executorService.execute(() -> {
            try {
                // Extract the file name from the URL
                String path = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                BlobId blobId = BlobId.of(BUCKET_NAME, path);
                File localFile = new File(context.getFilesDir(), fileName);
                
                // Download the file
                byte[] content = storage.readAllBytes(blobId);
                try (FileOutputStream fos = new FileOutputStream(localFile)) {
                    fos.write(content);
                }
                
                successListener.onSuccess(localFile);
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image", e);
                failureListener.onFailure(e);
            }
        });
    }

    public void cleanup() {
        executorService.shutdown();
    }
} 