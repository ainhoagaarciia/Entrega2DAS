package com.example.migym.workers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.migym.R;
import com.example.migym.notifications.WorkoutNotificationManager;
import com.example.migym.MainActivity;

public class WorkoutNotificationWorker extends Worker {
    public static final String KEY_WORKOUT_TITLE = "workout_title";
    public static final String KEY_NOTIFICATION_TYPE = "notification_type";
    public static final String TYPE_REMINDER = "reminder";
    public static final String TYPE_COMPLETION = "completion";

    private final Context context;

    public WorkoutNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String workoutTitle = getInputData().getString(KEY_WORKOUT_TITLE);
        String notificationType = getInputData().getString(KEY_NOTIFICATION_TYPE);

        if (workoutTitle == null || notificationType == null) {
            return Result.failure();
        }

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notification;
        if (TYPE_REMINDER.equals(notificationType)) {
            notification = createReminderNotification(workoutTitle, pendingIntent);
        } else if (TYPE_COMPLETION.equals(notificationType)) {
            notification = createCompletionNotification(workoutTitle, pendingIntent);
        } else {
            return Result.failure();
        }

        notificationManager.notify(WorkoutNotificationManager.NOTIFICATION_ID, notification.build());
        return Result.success();
    }

    private NotificationCompat.Builder createReminderNotification(
        String workoutTitle,
        PendingIntent pendingIntent
    ) {
        return new NotificationCompat.Builder(context, WorkoutNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.workout_reminder_title))
            .setContentText(context.getString(R.string.workout_reminder_text, workoutTitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
    }

    private NotificationCompat.Builder createCompletionNotification(
        String workoutTitle,
        PendingIntent pendingIntent
    ) {
        return new NotificationCompat.Builder(context, WorkoutNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.workout_completion_title))
            .setContentText(context.getString(R.string.workout_completion_text, workoutTitle))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
    }
} 