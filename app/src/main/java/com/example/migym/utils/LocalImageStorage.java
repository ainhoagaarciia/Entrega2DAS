package com.example.migym.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalImageStorage {
    private static final String TAG = "LocalImageStorage";
    private static final String IMAGE_DIR = "profile_images";
    private static final int MAX_IMAGE_DIMENSION = 1024; // Máxima dimensión permitida
    private static final int JPEG_QUALITY = 85; // Calidad de compresión JPEG
    private final Context context;

    public LocalImageStorage(Context context) {
        this.context = context;
    }

    public String saveImage(Uri imageUri) {
        try {
            // Crear el directorio si no existe
            File imageDir = new File(context.getFilesDir(), IMAGE_DIR);
            if (!imageDir.exists()) {
                if (!imageDir.mkdirs()) {
                    Log.e(TAG, "No se pudo crear el directorio de imágenes");
                    return null;
                }
            }

            // Generar un nombre único para la imagen
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "profile_" + timeStamp + ".jpg";
            File imageFile = new File(imageDir, imageFileName);

            // Copiar la imagen al archivo
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir el stream de la imagen");
                return null;
            }

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "Imagen guardada en: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error al guardar la imagen", e);
            return null;
        }
    }

    private void cleanupOldImages(File directory) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 5) { // Mantener máximo 5 imágenes
            // Ordenar por fecha de modificación
            java.util.Arrays.sort(files, (f1, f2) -> 
                Long.compare(f2.lastModified(), f1.lastModified()));
            
            // Eliminar las imágenes más antiguas
            for (int i = 5; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    public Uri getImageUri(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        File imageFile = new File(context.getFilesDir(), imagePath);
        if (imageFile.exists() && imageFile.length() > 0) {
            return Uri.fromFile(imageFile);
        }
        return null;
    }

    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            if (!imageFile.delete()) {
                Log.e(TAG, "No se pudo eliminar la imagen: " + imagePath);
            }
        }
    }

    public boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        File imageFile = new File(context.getFilesDir(), imagePath);
        return imageFile.exists() && imageFile.length() > 0;
    }
} 