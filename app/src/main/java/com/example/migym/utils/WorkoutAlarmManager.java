package com.example.migym.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.migym.notifications.WorkoutNotificationReceiver;
import com.example.migym.models.Workout;
import java.util.Calendar;

public class WorkoutAlarmManager {
    public static void scheduleWorkoutAlarm(Context context, Workout workout, int minutesBefore) {
        if (workout == null) return;
        android.util.Log.d("WorkoutAlarmManager", "scheduleWorkoutAlarm: id=" + workout.getId() + ", title=" + workout.getName() + ", time=" + workout.getTime() + ", minutesBefore=" + minutesBefore);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WorkoutNotificationReceiver.class);
        intent.putExtra("workout_title", workout.getName());
        intent.putExtra("workout_location", workout.getLocation());
        // Puedes añadir más extras si lo necesitas

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                workout.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calcular la hora de la alarma
        Calendar calendar = Calendar.getInstance();
        String[] timeParts = workout.getTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Restar los minutos previos
        calendar.add(Calendar.MINUTE, -minutesBefore);

        long triggerAtMillis = calendar.getTimeInMillis();
        if (triggerAtMillis < System.currentTimeMillis()) {
            // Si la hora ya pasó hoy, programa para el día siguiente
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            triggerAtMillis = calendar.getTimeInMillis();
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    public static void cancelWorkoutAlarm(Context context, Workout workout) {
        if (workout == null) return;
        android.util.Log.d("WorkoutAlarmManager", "cancelWorkoutAlarm: id=" + workout.getId() + ", title=" + workout.getName());
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WorkoutNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                workout.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
} 