package com.example.migym.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.net.UnknownHostException;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000; // 1 segundo entre reintentos
    
    // Puertos comunes para servidores de desarrollo
    private static final String[] DEV_PORTS = new String[] {
        "http://10.0.2.2:8080/",  // Spring Boot default
        "http://10.0.2.2:3000/",  // Node.js/Express common
        "http://10.0.2.2:5000/",  // Flask/Python common
        "http://10.0.2.2:4000/"   // Alternative Node.js
    };
    
    // URL para producción
    private static final String PROD_URL = "https://api.migym.com/";
    
    private static Retrofit retrofit = null;
    private static boolean isDebug = false;
    private static int currentPortIndex = 0;
    private static int currentRetryCount = 0;
    private static int totalAttempts = 0;

    public static void init(Context context) {
        isDebug = true; // Forzar modo debug para desarrollo
        Log.d(TAG, "ApiClient inicializado en modo: " + (isDebug ? "DEBUG" : "PRODUCCIÓN"));
        resetAttempts();
        
        // Intentar conectar al primer puerto al inicializar
        try {
            getClient();
            Log.d(TAG, "Conexión inicial exitosa");
        } catch (Exception e) {
            Log.e(TAG, "Error en la conexión inicial: " + e.getMessage());
            handleConnectionError(e);
        }
    }

    private static void resetAttempts() {
        currentRetryCount = 0;
        totalAttempts = 0;
        currentPortIndex = 0;
    }

    private static void handleConnectionError(Exception e) {
        totalAttempts++;
        
        // Si hemos intentado todos los puertos el número máximo de veces
        if (totalAttempts >= DEV_PORTS.length * MAX_RETRIES) {
            Log.e(TAG, "Se han agotado todos los intentos en todos los puertos");
            resetAttempts();
            throw new RuntimeException("No se pudo establecer conexión con ningún puerto después de " + 
                                    (DEV_PORTS.length * MAX_RETRIES) + " intentos");
        }

        // Si hemos intentado el puerto actual el máximo número de veces
        if (currentRetryCount >= MAX_RETRIES) {
            currentRetryCount = 0;
            tryNextPort();
        } else {
            currentRetryCount++;
            Log.d(TAG, String.format("Reintentando conexión %d/%d en puerto %s", 
                  currentRetryCount, MAX_RETRIES, DEV_PORTS[currentPortIndex]));
        }

        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException ie) {
            Log.e(TAG, "Error durante el retraso entre reintentos", ie);
        }
    }

    private static String getErrorMessage(ResponseBody errorBody) {
        try {
            if (errorBody != null) {
                return errorBody.string();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al leer el errorBody", e);
        }
        return "Error desconocido";
    }

    public static void tryNextPort() {
        currentPortIndex = (currentPortIndex + 1) % DEV_PORTS.length;
        resetClient();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Iniciando configuración de ApiClient...");
            
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> 
                Log.d(TAG, "API Log: " + message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        String url = chain.request().url().toString();
                        Log.d(TAG, "Iniciando petición a: " + url);
                        try {
                            okhttp3.Response response = chain.proceed(chain.request());
                            Log.d(TAG, "Respuesta recibida de " + url + ": " + response.code());
                            if (!response.isSuccessful()) {
                                try {
                                    if (response.body() != null) {
                                        String errorMessage = response.body().string();
                                        Log.e(TAG, "Error en la respuesta: " + errorMessage);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Error al leer el cuerpo de la respuesta", e);
                                }
                            }
                            return response;
                        } catch (Exception e) {
                            Log.e(TAG, "Error al conectar con " + url + ". Causa: " + e.getMessage());
                            handleConnectionError(e);
                            throw e;
                        }
                    })
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .writeTimeout(3, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true);

            OkHttpClient client = clientBuilder.build();
            String baseUrl = isDebug ? DEV_PORTS[currentPortIndex] : PROD_URL;
            Log.d(TAG, "Intentando conectar a: " + baseUrl);

            try {
                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                
                Log.d(TAG, "Retrofit client creado exitosamente para " + baseUrl);
                currentRetryCount = 0; // Resetear contador de reintentos si la conexión es exitosa
            } catch (Exception e) {
                Log.e(TAG, "Error al crear el cliente Retrofit para " + baseUrl, e);
                handleConnectionError(e);
                throw e;
            }
        }
        return retrofit;
    }

    public static void resetClient() {
        retrofit = null;
        Log.d(TAG, "ApiClient reseteado. Próximo intento usará: " + 
            (isDebug ? DEV_PORTS[currentPortIndex] : PROD_URL));
    }
} 