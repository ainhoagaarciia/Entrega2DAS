package com.example.migym.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import androidx.loader.content.CursorLoader;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private final Cloudinary cloudinary;
    // TODO: Reemplazar con tus credenciales de Cloudinary
    private static final String CLOUD_NAME = "tu_cloud_name";
    private static final String API_KEY = "tu_api_key";
    private static final String API_SECRET = "tu_api_secret";

    public CloudinaryManager() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);
        cloudinary = new Cloudinary(config);
    }

    public void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        try {
            String filePath = getRealPathFromURI(context, imageUri);
            if (filePath == null) {
                callback.onError("No se pudo obtener la ruta del archivo");
                return;
            }

            // Ejecutar la subida en un hilo secundario
            new Thread(() -> {
                try {
                    File file = new File(filePath);
                    Map<String, Object> options = new HashMap<>();
                    options.put("resource_type", "auto");
                    
                    // Subir la imagen
                    Map uploadResult = cloudinary.uploader().upload(file, options);
                    String imageUrl = (String) uploadResult.get("secure_url");
                    
                    // Volver al hilo principal para la callback
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(imageUrl);
                    });

                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onError(e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // Interfaz para manejar callbacks
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    // Método auxiliar para obtener la ruta real del archivo
    private String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        if (cursor == null) return null;
        
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
} 