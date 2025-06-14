package com.example.migym.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.migym.utils.NotificationHelper;

public class WorkoutNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String workoutTitle = intent.getStringExtra("workout_title");
        String workoutLocation = intent.getStringExtra("workout_location");
        android.util.Log.d("WorkoutNotificationReceiver", "onReceive: title=" + workoutTitle + ", location=" + workoutLocation);
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showWorkoutReminder(workoutTitle, workoutLocation);
    }
} 