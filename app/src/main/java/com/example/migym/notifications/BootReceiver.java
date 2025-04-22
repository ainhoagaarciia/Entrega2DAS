package com.example.migym.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Application;
import com.example.migym.data.AppDatabase;
import com.example.migym.data.WorkoutDao;
import com.example.migym.models.Workout;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AppDatabase db = AppDatabase.getInstance(context);
            WorkoutDao workoutDao = db.workoutDao();
            WorkoutNotificationManager notificationManager = new WorkoutNotificationManager((Application) context.getApplicationContext());

            List<Workout> workouts = workoutDao.getAllWorkoutsSync();
            for (Workout workout : workouts) {
                notificationManager.scheduleWorkoutNotification(workout);
            }
        }
    }
} 