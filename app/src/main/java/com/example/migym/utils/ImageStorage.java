package com.example.migym.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageStorage {
    private static final String TAG = "ImageStorage";
    private static final int MAX_IMAGE_SIZE = 1024; // 1MB
    private static final String PROFILE_IMAGE_DIR = "profile_images";
    private static final String PROFILE_IMAGE_NAME = "profile.jpg";

    private final Context context;
    private final File imageDir;

    public interface ImageStorageListener {
        void onSuccess(byte[] imageData);
        void onError(String error);
    }

    public ImageStorage(Context context) {
        this.context = context.getApplicationContext();
        this.imageDir = new File(context.getFilesDir(), PROFILE_IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
    }

    public void saveImage(@NonNull Uri imageUri, @NonNull ImageStorageListener listener) {
        try {
            // Read and compress the image
            byte[] imageData = getCompressedImageData(imageUri);
            if (imageData == null) {
                listener.onError("Failed to process image");
                return;
            }

            // Save to internal storage
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(context.getFilesDir(), fileName);
            
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageData);
                listener.onSuccess(imageData);
            } catch (IOException e) {
                Log.e(TAG, "Error saving image", e);
                listener.onError("Error saving image: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            listener.onError("Error processing image: " + e.getMessage());
        }
    }

    private byte[] getCompressedImageData(Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            return null;
        }

        // Decode the image
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        if (originalBitmap == null) {
            return null;
        }

        // Calculate compression ratio
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int quality = 100;
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        
        while (outputStream.toByteArray().length > MAX_IMAGE_SIZE && quality > 10) {
            outputStream.reset();
            quality -= 10;
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }

        originalBitmap.recycle();
        return outputStream.toByteArray();
    }

    public void deleteImage(String fileName) {
        File imageFile = new File(context.getFilesDir(), fileName);
        if (imageFile.exists()) {
            if (!imageFile.delete()) {
                Log.e(TAG, "Failed to delete image file: " + fileName);
            }
        }
    }

    public String saveProfileImage(Uri imageUri) {
        File imageFile = new File(imageDir, PROFILE_IMAGE_NAME);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving profile image", e);
            return null;
        }
    }

    public String saveProfileImage(Bitmap bitmap) {
        File imageFile = new File(imageDir, PROFILE_IMAGE_NAME);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving profile image", e);
            return null;
        }
    }

    public Bitmap loadProfileImage() {
        File imageFile = new File(imageDir, PROFILE_IMAGE_NAME);
        if (!imageFile.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
    }

    public void deleteProfileImage() {
        File imageFile = new File(imageDir, PROFILE_IMAGE_NAME);
        if (imageFile.exists()) {
            imageFile.delete();
        }
    }

    public boolean hasProfileImage() {
        File imageFile = new File(imageDir, PROFILE_IMAGE_NAME);
        return imageFile.exists();
    }

    public String getProfileImagePath() {
        File imageFile = new File(imageDir, PROFILE_IMAGE_NAME);
        return imageFile.exists() ? imageFile.getAbsolutePath() : null;
    }
} 