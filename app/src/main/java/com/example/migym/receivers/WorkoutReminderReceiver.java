package com.example.migym.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.migym.utils.NotificationHelper;

public class WorkoutReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String workoutTitle = intent.getStringExtra("workout_title");
        String workoutLocation = intent.getStringExtra("workout_location");

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showWorkoutReminder(workoutTitle, workoutLocation);
    }
} 