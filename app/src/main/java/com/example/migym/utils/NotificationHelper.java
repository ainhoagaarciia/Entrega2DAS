package com.example.migym.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.migym.R;
import com.example.migym.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "workout_reminder_channel";
    private static final int NOTIFICATION_ID = 1;
    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.workout_reminder_title),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.workout_reminder_content));
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showWorkoutReminder(String workoutName, String location) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.workout_reminder_title))
                .setContentText(context.getString(R.string.workout_reminder_text, workoutName))
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.workout_reminder_text, workoutName) + 
                            (location != null ? "\n" + location : "")))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            android.util.Log.d("NotificationHelper", "Notification sent successfully");
        } catch (Exception e) {
            android.util.Log.e("NotificationHelper", "Error showing notification: " + e.getMessage());
        }
    }

    public void cancelWorkoutReminder(String workoutId) {
        notificationManager.cancel(workoutId.hashCode());
    }

    public void cancelAllReminders() {
        notificationManager.cancelAll();
    }
} 