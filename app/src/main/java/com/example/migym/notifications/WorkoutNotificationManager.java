package com.example.migym.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.migym.R;
import com.example.migym.models.Workout;
import com.example.migym.MainActivity;
import com.example.migym.workers.WorkoutNotificationWorker;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WorkoutNotificationManager {
    public static final String CHANNEL_ID = "workout_notifications";
    private static final String CHANNEL_NAME = "Workout Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for workout reminders and completion";
    public static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManager notificationManager;
    private final WorkManager workManager;

    public WorkoutNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.workManager = WorkManager.getInstance(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleWorkoutNotification(Workout workout) {
        if (!workout.isNotificationEnabled()) {
            return;
        }

        // Cancel any existing notifications for this workout
        cancelWorkoutNotification(workout.getId());

        // Schedule reminder notification
        Data reminderData = new Data.Builder()
            .putString(WorkoutNotificationWorker.KEY_WORKOUT_TITLE, workout.getTitle())
            .putString(WorkoutNotificationWorker.KEY_NOTIFICATION_TYPE, WorkoutNotificationWorker.TYPE_REMINDER)
            .build();

        OneTimeWorkRequest reminderRequest = new OneTimeWorkRequest.Builder(WorkoutNotificationWorker.class)
            .setInputData(reminderData)
            .setInitialDelay(calculateReminderDelay(workout), TimeUnit.MILLISECONDS)
            .build();

        workManager.enqueueUniqueWork(
            "reminder_" + workout.getId(),
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        );

        // Schedule completion notification
        Data completionData = new Data.Builder()
            .putString(WorkoutNotificationWorker.KEY_WORKOUT_TITLE, workout.getTitle())
            .putString(WorkoutNotificationWorker.KEY_NOTIFICATION_TYPE, WorkoutNotificationWorker.TYPE_COMPLETION)
            .build();

        OneTimeWorkRequest completionRequest = new OneTimeWorkRequest.Builder(WorkoutNotificationWorker.class)
            .setInputData(completionData)
            .setInitialDelay(calculateCompletionDelay(workout), TimeUnit.MILLISECONDS)
            .build();

        workManager.enqueueUniqueWork(
            "completion_" + workout.getId(),
            ExistingWorkPolicy.REPLACE,
            completionRequest
        );
    }

    private long calculateReminderDelay(Workout workout) {
        long currentTime = System.currentTimeMillis();
        long workoutTime = getWorkoutTimeInMillis(workout);
        long reminderTime = workoutTime - TimeUnit.MINUTES.toMillis(workout.getNotificationTime());
        return Math.max(0L, reminderTime - currentTime);
    }

    private long calculateCompletionDelay(Workout workout) {
        long currentTime = System.currentTimeMillis();
        long workoutTime = getWorkoutTimeInMillis(workout);
        long completionTime = workoutTime + TimeUnit.MINUTES.toMillis(workout.getDuration());
        return Math.max(0L, completionTime - currentTime);
    }

    private long getWorkoutTimeInMillis(Workout workout) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, workout.getDayOfWeek());
        
        String[] timeParts = workout.getTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for next week
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }

    public void cancelWorkoutNotification(String workoutId) {
        if (workoutId == null) return;
        workManager.cancelUniqueWork("reminder_" + workoutId);
        workManager.cancelUniqueWork("completion_" + workoutId);
    }

    public void cancelAllNotifications() {
        workManager.cancelAllWork();
    }
}