package com.example.migym.utils;

/**
 * Clase de configuración para almacenar constantes y configuraciones de la aplicación
 */
public class AppConfig {
    // URL del servidor para subir imágenes
    public static final String SERVER_URL = "https://tu-servidor.com";
    
    // Endpoints del servidor
    public static final String UPLOAD_ENDPOINT = "/upload";
    public static final String IMAGES_ENDPOINT = "/images";
    
    // Tamaño máximo de imagen (en bytes)
    public static final int MAX_IMAGE_SIZE = 1024 * 1024; // 1MB
    
    // Calidad de compresión de imagen
    public static final int IMAGE_COMPRESSION_QUALITY = 80;
    
    // Tiempo de espera para conexiones (en milisegundos)
    public static final int CONNECTION_TIMEOUT = 30000; // 30 segundos
    public static final int READ_TIMEOUT = 30000; // 30 segundos
} 