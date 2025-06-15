# Memoria del Proyecto MiGym

## 1. Enlace al Repositorio
El proyecto completo se encuentra disponible en el siguiente repositorio de GitHub:
https://github.com/ainhoagaarciia/Entrega2DAS

## 2. Elementos de Valoración y Funcionalidades

### 2.1 Gestión de Usuarios
- Registro y autenticación de usuarios mediante Firebase Authentication
- Perfil de usuario con información personal y preferencias
- Almacenamiento de datos de usuario en Firestore y Room Database
- Gestión de imágenes de perfil con Firebase Storage

### 2.2 Gestión de Entrenamientos
- Creación y edición de entrenamientos personalizados
- Categorización por tipo de ejercicio
- Programación de entrenamientos con notificaciones
- Seguimiento del progreso y estadísticas
- Almacenamiento local y sincronización con Firebase

### 2.3 Características Adicionales
- Widgets para acceso rápido a entrenamientos
- Notificaciones programadas para recordatorios
- Integración con Google Maps para ubicaciones
- Soporte multiidioma (español e inglés)
- Tema claro/oscuro

## 3. Estructura de la Aplicación

### 3.1 Diagrama de Clases
```
[Diagrama de clases en formato UML]
- User
  - id: String
  - name: String
  - email: String
  - preferences: UserPreferences
  - ...

- Workout
  - id: String
  - name: String
  - type: String
  - date: Date
  - duration: int
  - ...

- UserPreferences
  - language: String
  - theme: String
  - notifications: boolean
  - ...

- WorkoutRepository
  - createWorkout()
  - updateWorkout()
  - deleteWorkout()
  - ...

- UserRepository
  - updateProfile()
  - uploadPhoto()
  - syncData()
  - ...
```

### 3.2 Base de Datos
La aplicación utiliza dos sistemas de almacenamiento:

#### Room Database (Local)
- Tabla: users
  - Campos: id, name, email, preferences, etc.
- Tabla: workouts
  - Campos: id, name, type, date, duration, etc.

#### Firebase (Cloud)
- Colección: users
  - Documentos con datos de usuario
- Colección: workouts
  - Documentos con información de entrenamientos
- Storage: Imágenes de perfil y entrenamientos

## 4. Manual de Usuario

### 4.1 Requisitos
- Android 6.0 o superior
- Conexión a Internet para sincronización
- Permisos de ubicación para mapas
- Permisos de cámara para fotos de perfil

### 4.2 Inicio de Sesión
1. Abrir la aplicación
2. Seleccionar "Registrarse" si es nuevo usuario
3. Completar el formulario de registro
4. O iniciar sesión con email/contraseña existente

### 4.3 Funcionalidades Principales
1. **Gestión de Perfil**
   - Acceder a "Mi Perfil"
   - Editar información personal
   - Cambiar foto de perfil
   - Configurar preferencias

2. **Entrenamientos**
   - Ver lista de entrenamientos
   - Crear nuevo entrenamiento
   - Editar entrenamientos existentes
   - Marcar como completados

3. **Notificaciones**
   - Configurar recordatorios
   - Recibir notificaciones de entrenamientos
   - Gestionar preferencias de notificación

4. **Widgets**
   - Añadir widget de entrenamientos
   - Añadir widget de saludo
   - Personalizar widgets

## 5. Dificultades Encontradas

1. **Sincronización de Datos**
   - Desafíos en la sincronización entre Room y Firebase
   - Manejo de conflictos en datos offline/online
   - Solución: Implementación de sistema de colas y timestamps

2. **Gestión de Permisos**
   - Complejidad en la gestión de permisos en Android
   - Adaptación a diferentes versiones de Android
   - Solución: Implementación de sistema de permisos dinámico

3. **Rendimiento**
   - Optimización de carga de imágenes
   - Manejo de memoria en listas largas
   - Solución: Implementación de paginación y caché

4. **Notificaciones**
   - Compatibilidad con diferentes versiones de Android
   - Gestión de notificaciones en segundo plano
   - Solución: Uso de WorkManager y AlarmManager

## 6. Fuentes Utilizadas

1. **Documentación Oficial**
   - Android Developer Documentation
   - Firebase Documentation
   - Room Persistence Library Guide

2. **Librerías y Frameworks**
   - Firebase (Authentication, Firestore, Storage)
   - Room Database
   - WorkManager
   - Glide
   - Retrofit
   - Google Maps SDK

3. **Recursos de Aprendizaje**
   - Android Jetpack Components
   - Material Design Guidelines
   - Android Architecture Components

4. **Herramientas de Desarrollo**
   - Android Studio
   - Git
   - Firebase Console
   - Google Cloud Platform

## 7. Detalle de Implementación de Elementos de Valoración

### 7.1 Base de datos remota y registro de usuarios ✅
- **Ubicación**: 
  - `app/src/main/java/com/example/migym/repositories/UserRepository.java`
  - `app/src/main/java/com/example/migym/ui/auth/LoginActivity.java`
  - `app/src/main/java/com/example/migym/MainActivity.java`
- **Implementación**: Firebase Authentication y Firestore para el registro y autenticación de usuarios

### 7.2 Google Maps y Geolocalización ✅
- **Ubicación**: 
  - `app/src/main/java/com/example/migym/ui/MapActivity.java`
  - `app/src/main/res/layout/activity_map.xml`
- **Implementación**: 
  - Integración completa de Google Maps
  - Geolocalización con FusedLocationProviderClient
  - Permisos de ubicación en AndroidManifest.xml

### 7.3 Captura y gestión de imágenes ✅
- **Ubicación**:
  - `app/src/main/java/com/example/migym/ui/profile/ProfileFragment.java`
  - `app/src/main/java/com/example/migym/utils/FirebaseStorageManager.java`
  - `app/src/main/res/xml/file_paths.xml`
- **Implementación**:
  - Captura de fotos con la cámara
  - Almacenamiento en Firebase Storage
  - Gestión de permisos de cámara y almacenamiento

### 7.4 Content Provider ✅
- **Ubicación**:
  - `app/src/main/AndroidManifest.xml` (FileProvider)
  - `app/src/main/res/xml/file_paths.xml`
- **Implementación**: FileProvider para compartir archivos entre aplicaciones

### 7.5 Servicio en primer plano y Broadcast Receivers ✅
- **Ubicación**:
  - `app/src/main/java/com/example/migym/services/NotificationService.java`
  - `app/src/main/java/com/example/migym/receivers/NotificationReceiver.java`
  - `app/src/main/java/com/example/migym/notifications/WorkoutNotificationReceiver.java`
  - `app/src/main/java/com/example/migym/notifications/BootReceiver.java`
- **Implementación**: Servicios y receptores para notificaciones

### 7.6 Widgets con actualización automática ✅
- **Ubicación**:
  - `app/src/main/java/com/example/migym/widgets/WorkoutWidget.java`
  - `app/src/main/java/com/example/migym/widgets/GreetingWidget.java`
- **Implementación**: Widgets con actualización periódica

### 7.7 Tareas programadas mediante alarmas ✅
- **Ubicación**:
  - `app/src/main/java/com/example/migym/utils/WorkoutAlarmManager.java`
  - `app/src/main/java/com/example/migym/notifications/WorkoutNotificationReceiver.java`
- **Implementación**: Sistema de alarmas para notificaciones de entrenamientos

### 7.8 Mensajería FCM ❌
- **Estado**: No implementado completamente
- **Nota**: Aunque hay estructura para FCM (token en modelo de usuario), no está implementada la funcionalidad completa de mensajería push ni el servicio web PHP para pruebas. 

## 8. Funcionamiento de la Aplicación

### 8.1 Flujo Principal de la Aplicación

1. **Inicio y Autenticación**
   - La aplicación inicia en `LoginActivity`
   - Los usuarios pueden registrarse o iniciar sesión usando Firebase Authentication
   - Después de la autenticación exitosa, se redirige a `MainActivity`

2. **Pantalla Principal (MainActivity)**
   - Muestra un menú de navegación con las siguientes opciones:
     - Inicio: Resumen de entrenamientos y estadísticas
     - Entrenamientos: Lista de entrenamientos programados
     - Perfil: Información y configuración del usuario
     - Mapa: Ubicaciones de entrenamientos

3. **Gestión de Entrenamientos**
   - Los usuarios pueden:
     - Crear nuevos entrenamientos con detalles como:
       - Nombre y descripción
       - Tipo de ejercicio
       - Duración
       - Dificultad
       - Ubicación (usando Google Maps)
     - Editar entrenamientos existentes
     - Marcar entrenamientos como completados
     - Recibir notificaciones de recordatorio

4. **Sistema de Notificaciones**
   - Las notificaciones se gestionan mediante:
     - `NotificationService`: Servicio en primer plano
     - `WorkoutNotificationReceiver`: Maneja las notificaciones de entrenamientos
     - `WorkoutAlarmManager`: Programa las alarmas para las notificaciones

5. **Gestión de Perfil**
   - Los usuarios pueden:
     - Actualizar información personal
     - Subir foto de perfil (usando cámara o galería)
     - Configurar preferencias de la aplicación
     - Gestionar notificaciones

### 8.2 Características Técnicas

1. **Almacenamiento de Datos**
   - **Local (Room Database)**:
     - Almacena datos de usuario y entrenamientos
     - Permite funcionamiento offline
     - Sincroniza con Firebase cuando hay conexión
   
   - **Remoto (Firebase)**:
     - Firestore: Almacena datos de usuario y entrenamientos
     - Storage: Guarda imágenes de perfil
     - Authentication: Gestiona usuarios

2. **Sincronización**
   - Sistema de colas para sincronización
   - Manejo de conflictos offline/online
   - Actualización automática de widgets

3. **Widgets**
   - **WorkoutWidget**:
     - Muestra próximos entrenamientos
     - Actualización automática cada 30 minutos
     - Acceso rápido a detalles de entrenamiento
   
   - **GreetingWidget**:
     - Muestra saludo personalizado
     - Actualiza según la hora del día
     - Muestra estadísticas básicas

4. **Integración con Google Maps**
   - Selección de ubicaciones para entrenamientos
   - Visualización de entrenamientos en el mapa
   - Cálculo de rutas y distancias

### 8.3 Flujo de Datos

1. **Creación de Entrenamiento**
   ```
   Usuario → Interfaz → WorkoutRepository → Room DB → Firebase
   ```

2. **Actualización de Perfil**
   ```
   Usuario → Interfaz → UserRepository → Room DB → Firebase Storage/Firestore
   ```

3. **Notificaciones**
   ```
   WorkoutAlarmManager → WorkoutNotificationReceiver → NotificationService → Usuario
   ```

4. **Sincronización**
   ```
   Cambios Locales → Cola de Sincronización → Firebase → Actualización UI
   ```

### 8.4 Características de Seguridad

1. **Autenticación**
   - Sistema seguro de login/registro
   - Tokens de sesión
   - Recuperación de contraseña

2. **Permisos**
   - Gestión dinámica de permisos
   - Solicitud de permisos en tiempo real
   - Adaptación a diferentes versiones de Android

3. **Almacenamiento**
   - Datos sensibles encriptados
   - Reglas de seguridad en Firebase
   - Validación de datos

### 8.5 Optimizaciones

1. **Rendimiento**
   - Caché de imágenes
   - Paginación en listas
   - Carga lazy de datos

2. **Batería**
   - Optimización de servicios en segundo plano
   - Gestión eficiente de alarmas
   - Reducción de actualizaciones innecesarias

3. **Datos**
   - Compresión de imágenes
   - Sincronización selectiva
   - Limpieza periódica de caché 

## 9. Diagrama de Clases Detallado

### 9.1 Entidades Principales

```
[User]
- id: String
- name: String
- email: String
- phone: String
- photoUrl: String
- weight: double
- height: double
- age: int
- heartProblems: boolean
- heartProblemsDetails: String
- role: String
- fcmToken: String
- lastLogin: Timestamp
- isActive: boolean
- preferences: UserPreferences
- userId: String
- gender: int
+ User()
+ getId(): String
+ setId(id: String): void
+ getName(): String
+ setName(name: String): void
+ getEmail(): String
+ setEmail(email: String): void
+ getPhone(): String
+ setPhone(phone: String): void
+ getPhotoUrl(): String
+ setPhotoUrl(photoUrl: String): void
+ getWeight(): double
+ setWeight(weight: double): void
+ getHeight(): double
+ setHeight(height: double): void
+ getAge(): int
+ setAge(age: int): void
+ isHeartProblems(): boolean
+ setHeartProblems(heartProblems: boolean): void
+ getHeartProblemsDetails(): String
+ setHeartProblemsDetails(details: String): void
+ getRole(): String
+ setRole(role: String): void
+ getFcmToken(): String
+ setFcmToken(token: String): void
+ getLastLogin(): Timestamp
+ setLastLogin(lastLogin: Timestamp): void
+ isActive(): boolean
+ setActive(active: boolean): void
+ getPreferences(): UserPreferences
+ setPreferences(preferences: UserPreferences): void
+ getUserId(): String
+ setUserId(userId: String): void
+ getGender(): int
+ setGender(gender: int): void
```

```
[Workout]
- id: String
- name: String
- title: String
- description: String
- type: String
- dayOfWeek: int
- time: String
- duration: int
- instructor: String
- location: String
- completed: int
- difficulty: String
- equipment: String
- muscleGroups: String
- imageUrl: String
- latitude: double
- longitude: double
- distance: double
- speed: double
- notes: String
- performance: int
- notificationTime: int
- notificationEnabled: boolean
+ Workout()
+ Workout(name, type, dayOfWeek, time)
+ getId(): String
+ setId(id: String): void
+ getName(): String
+ setName(name: String): void
+ getTitle(): String
+ setTitle(title: String): void
+ getDescription(): String
+ setDescription(description: String): void
+ getType(): String
+ setType(type: String): void
+ getDayOfWeek(): int
+ setDayOfWeek(day: int): void
+ getTime(): String
+ setTime(time: String): void
+ getDuration(): int
+ setDuration(duration: int): void
+ getInstructor(): String
+ setInstructor(instructor: String): void
+ getLocation(): String
+ setLocation(location: String): void
+ getCompleted(): int
+ setCompleted(completed: int): void
+ getDifficulty(): String
+ setDifficulty(difficulty: String): void
+ getEquipment(): String
+ setEquipment(equipment: String): void
+ getMuscleGroups(): String
+ setMuscleGroups(groups: String): void
+ getImageUrl(): String
+ setImageUrl(url: String): void
+ getLatitude(): double
+ setLatitude(latitude: double): void
+ getLongitude(): double
+ setLongitude(longitude: double): void
+ getDistance(): double
+ setDistance(distance: double): void
+ getSpeed(): double
+ setSpeed(speed: double): void
+ getNotes(): String
+ setNotes(notes: String): void
+ getPerformance(): int
+ setPerformance(performance: int): void
+ getNotificationTime(): int
+ setNotificationTime(time: int): void
+ isNotificationEnabled(): boolean
+ setNotificationEnabled(enabled: boolean): void
```

```
[UserPreferences]
- language: String
- theme: String
- notifications: boolean
- notificationTime: int
- measurementSystem: String
+ UserPreferences()
+ getLanguage(): String
+ setLanguage(language: String): void
+ getTheme(): String
+ setTheme(theme: String): void
+ isNotifications(): boolean
+ setNotifications(notifications: boolean): void
+ getNotificationTime(): int
+ setNotificationTime(time: int): void
+ getMeasurementSystem(): String
+ setMeasurementSystem(system: String): void
```

### 9.2 Repositorios

```
[UserRepository]
- auth: FirebaseAuth
- db: FirebaseFirestore
- storage: FirebaseStorage
+ UserRepository(context)
+ registerUser(email: String, password: String): Task<AuthResult>
+ loginUser(email: String, password: String): Task<AuthResult>
+ updateProfile(user: User): Task<Void>
+ uploadPhoto(uri: Uri): Task<Uri>
+ syncData(): Task<Void>
+ getUser(): LiveData<User>
+ updateUserPreferences(preferences: UserPreferences): Task<Void>
+ deleteAccount(): Task<Void>
+ signOut(): void
```

```
[WorkoutRepository]
- db: FirebaseFirestore
- workoutDao: WorkoutDao
+ WorkoutRepository(context)
+ createWorkout(workout: Workout): Task<Void>
+ updateWorkout(workout: Workout): Task<Void>
+ deleteWorkout(workout: Workout): Task<Void>
+ getWorkouts(): LiveData<List<Workout>>
+ syncWorkouts(): Task<Void>
+ getWorkoutById(id: String): LiveData<Workout>
+ getWorkoutsByDate(date: Date): LiveData<List<Workout>>
+ getWorkoutsByType(type: String): LiveData<List<Workout>>
```

### 9.3 Servicios y Utilidades

```
[NotificationService]
- notificationManager: NotificationManager
- context: Context
+ NotificationService()
+ showNotification(title: String, message: String): void
+ scheduleWorkoutNotification(workout: Workout): void
+ cancelNotification(id: int): void
+ createNotificationChannel(): void
+ updateNotification(workout: Workout): void
+ isNotificationEnabled(): boolean
+ setNotificationEnabled(enabled: boolean): void
```

```
[WorkoutAlarmManager]
- alarmManager: AlarmManager
- context: Context
+ WorkoutAlarmManager(context)
+ scheduleWorkoutAlarm(workout: Workout): void
+ cancelWorkoutAlarm(workout: Workout): void
+ rescheduleAllAlarms(): void
+ isAlarmScheduled(workout: Workout): boolean
+ getNextAlarmTime(workout: Workout): long
```

```
[FirebaseStorageManager]
- storage: FirebaseStorage
- storageRef: StorageReference
+ FirebaseStorageManager()
+ uploadImage(uri: Uri, path: String): Task<Uri>
+ getImageUrl(path: String): Task<String>
+ deleteImage(path: String): Task<Void>
+ compressImage(uri: Uri): Bitmap
+ getImageSize(uri: Uri): long
```

### 9.4 ViewModels

```
[UserViewModel]
- userRepository: UserRepository
- user: LiveData<User>
+ UserViewModel()
+ updateUserProfile(user: User): void
+ uploadProfilePhoto(uri: Uri): void
+ syncUserData(): void
+ getUser(): LiveData<User>
+ updatePreferences(preferences: UserPreferences): void
+ deleteAccount(): void
+ signOut(): void
```

```
[WorkoutViewModel]
- workoutRepository: WorkoutRepository
- workouts: LiveData<List<Workout>>
+ WorkoutViewModel()
+ createWorkout(workout: Workout): void
+ updateWorkout(workout: Workout): void
+ deleteWorkout(workout: Workout): void
+ getWorkouts(): LiveData<List<Workout>>
+ getWorkoutById(id: String): LiveData<Workout>
+ getWorkoutsByDate(date: Date): LiveData<List<Workout>>
+ getWorkoutsByType(type: String): LiveData<List<Workout>>
+ syncWorkouts(): void
```

### 9.5 Relaciones entre Clases

```
User 1--1 UserPreferences
User 1--* Workout
UserRepository *--1 User
WorkoutRepository *--* Workout
UserViewModel *--1 UserRepository
WorkoutViewModel *--1 WorkoutRepository
NotificationService *--1 WorkoutAlarmManager
FirebaseStorageManager *--1 User
```

### 9.6 Interfaces y Callbacks

```
[OnUploadListener]
+ onSuccess(downloadUrl: String)
+ onFailure(exception: Exception)
```

```
[OnImageUrlListener]
+ onSuccess(url: String)
+ onFailure(exception: Exception)
```

```
[OnWorkoutListener]
+ onWorkoutCreated(workout: Workout)
+ onWorkoutUpdated(workout: Workout)
+ onWorkoutDeleted(workout: Workout)
+ onError(exception: Exception)
```

### 9.7 Notas para el Diagrama Visual

1. **Colores Sugeridos**:
   - Entidades: Azul claro
   - Repositorios: Verde claro
   - Servicios: Amarillo claro
   - ViewModels: Naranja claro
   - Interfaces: Gris claro

2. **Tipos de Relaciones**:
   - Composición (1--1): Línea sólida con diamante relleno
   - Agregación (1--*): Línea sólida con diamante vacío
   - Dependencia: Línea punteada con flecha

3. **Elementos a Incluir**:
   - Nombre de la clase
   - Atributos con tipos
   - Métodos principales
   - Relaciones entre clases
   - Interfaces implementadas

4. **Organización**:
   - Entidades en la parte superior
   - Repositorios en el medio
   - Servicios y ViewModels en la parte inferior
   - Interfaces en el lateral derecho

5. **Ejemplo de Uso**:
   - Mostrar cómo se relacionan las clases y cómo se utilizan para implementar la funcionalidad de la aplicación 

## 10. Conexiones y Relaciones entre Clases

### 10.1 Relaciones de Composición (1 a 1)
```
User ────────► UserPreferences
- Un usuario tiene exactamente un conjunto de preferencias
- Las preferencias no pueden existir sin un usuario
- Relación de dependencia fuerte
```

### 10.2 Relaciones de Agregación (1 a Muchos)
```
User ────────► Workout
- Un usuario puede tener múltiples entrenamientos
- Los entrenamientos pueden existir independientemente
- Relación de dependencia débil
```

### 10.3 Relaciones de Dependencia
```
UserRepository ────────► User
- El repositorio de usuario depende de la clase User
- Maneja las operaciones CRUD para usuarios
- No hay dependencia inversa

WorkoutRepository ────────► Workout
- El repositorio de entrenamientos depende de la clase Workout
- Maneja las operaciones CRUD para entrenamientos
- No hay dependencia inversa
```

### 10.4 Relaciones de Asociación
```
UserViewModel ────────► UserRepository
- El ViewModel utiliza el repositorio para acceder a datos
- Relación de dependencia para operaciones de datos
- Comunicación unidireccional

WorkoutViewModel ────────► WorkoutRepository
- El ViewModel utiliza el repositorio para acceder a datos
- Relación de dependencia para operaciones de datos
- Comunicación unidireccional
```

### 10.5 Relaciones de Servicio
```
NotificationService ────────► WorkoutAlarmManager
- El servicio de notificaciones utiliza el gestor de alarmas
- Relación de dependencia para programar notificaciones
- Comunicación unidireccional

FirebaseStorageManager ────────► User
- El gestor de almacenamiento maneja las imágenes de usuario
- Relación de dependencia para operaciones de almacenamiento
- Comunicación unidireccional
```

### 10.6 Diagrama de Dependencias
```
[UI Layer]
    │
    ▼
[ViewModels]
    │
    ▼
[Repositories]
    │
    ▼
[Services/Managers]
    │
    ▼
[Entities]
```

### 10.7 Flujo de Datos
```
1. Flujo de Creación de Entrenamiento:
   UI → WorkoutViewModel → WorkoutRepository → Room DB → Firebase

2. Flujo de Actualización de Perfil:
   UI → UserViewModel → UserRepository → Room DB → Firebase

3. Flujo de Notificaciones:
   WorkoutAlarmManager → NotificationService → UI

4. Flujo de Sincronización:
   Repositories → Room DB ↔ Firebase
```

### 10.8 Patrones de Diseño Utilizados

1. **MVVM (Model-View-ViewModel)**
   - View: Activities y Fragments
   - ViewModel: UserViewModel, WorkoutViewModel
   - Model: User, Workout, Repositories

2. **Repository Pattern**
   - UserRepository
   - WorkoutRepository
   - Abstracción de fuentes de datos

3. **Observer Pattern**
   - LiveData en ViewModels
   - Notificaciones y actualizaciones de UI

4. **Singleton Pattern**
   - FirebaseStorageManager
   - NotificationService
   - WorkoutAlarmManager

### 10.9 Comunicación entre Componentes

1. **UI ↔ ViewModel**
   - Databinding
   - LiveData
   - Eventos de usuario

2. **ViewModel ↔ Repository**
   - Llamadas a métodos
   - Tasks de Firebase
   - LiveData para observación

3. **Repository ↔ Firebase/Room**
   - Operaciones CRUD
   - Sincronización de datos
   - Manejo de errores

4. **Services ↔ Managers**
   - Programación de tareas
   - Gestión de recursos
   - Comunicación asíncrona

### 10.10 Notas de Implementación

1. **Inyección de Dependencias**
   - ViewModelProvider para ViewModels
   - Constructor injection para Repositories
   - Context injection para Services

2. **Manejo de Estados**
   - Estados de carga
   - Estados de error
   - Estados de éxito

3. **Sincronización**
   - Colas de operaciones
   - Manejo de conflictos
   - Actualización de UI

4. **Ciclo de Vida**
   - Observadores de ciclo de vida
   - Limpieza de recursos
   - Manejo de configuración 