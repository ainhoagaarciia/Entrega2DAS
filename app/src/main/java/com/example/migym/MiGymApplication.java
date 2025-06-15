package com.example.migym;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.example.migym.api.ApiClient;
import com.example.migym.data.AppDatabase;
import com.example.migym.utils.LocaleHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MiGymApplication extends Application {
    private static MiGymApplication instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        try {
            FirebaseApp.initializeApp(this);
            
            // Configurar Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
            db.setFirestoreSettings(settings);

            ApiClient.init(this);
            // Initialize the database
            AppDatabase.getInstance(this);
        } catch (Exception e) {
            Log.e("MiGymApplication", "Error initializing Firebase", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Clean up database instance
        AppDatabase.destroyInstance();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.onAttach(this);
    }

    public static MiGymApplication getInstance() {
        return instance;
    }
} 