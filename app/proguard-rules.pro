# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Mantener las clases de Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.TypeConverter class *

# Mantener las clases de WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class * extends androidx.work.WorkerFactory
-keep class * extends androidx.work.WorkerParameters

# Mantener las clases de la aplicación
-keep class com.example.migym.** { *; }
-keep class com.example.migym.data.** { *; }
-keep class com.example.migym.ui.** { *; }
-keep class com.example.migym.dialogs.** { *; }
-keep class com.example.migym.workers.** { *; }
-keep class com.example.migym.utils.** { *; }

# Mantener las clases de Parcelable
-keep class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Mantener las clases de Serializable
-keep class * implements java.io.Serializable

# Mantener las clases de ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
}

# Mantener las clases de R
-keep class **.R$* {
    *;
}