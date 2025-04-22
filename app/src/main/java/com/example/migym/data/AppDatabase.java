package com.example.migym.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.migym.models.User;
import com.example.migym.models.Workout;
import com.example.migym.models.DateConverter;
import com.example.migym.utils.TimestampConverter;
import com.example.migym.data.converters.UserPreferencesConverter;

@Database(entities = {Workout.class, User.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, TimestampConverter.class, UserPreferencesConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "migym_db";
    private static AppDatabase instance;

    public abstract WorkoutDao workoutDao();
    public abstract UserDao userDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE workouts ADD COLUMN notification_time INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE workouts ADD COLUMN notification_enabled INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public static synchronized void destroyInstance() {
        instance = null;
    }
} 