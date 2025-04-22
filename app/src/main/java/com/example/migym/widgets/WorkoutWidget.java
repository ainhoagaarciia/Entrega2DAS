package com.example.migym.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import com.example.migym.R;
import com.example.migym.models.Workout;
import com.example.migym.data.AppDatabase;
import com.example.migym.data.WorkoutDao;
import java.util.Calendar;
import java.util.List;

public class WorkoutWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.workout_widget);
        
        AppDatabase db = AppDatabase.getInstance(context);
        WorkoutDao workoutDao = db.workoutDao();
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        List<Workout> workouts = workoutDao.getWorkoutsByDaySync(today);
        
        if (!workouts.isEmpty()) {
            StringBuilder workoutText = new StringBuilder();
            for (Workout workout : workouts) {
                workoutText.append(workout.getTitle())
                          .append(" - ")
                          .append(workout.getTime())
                          .append("\n");
            }
            views.setTextViewText(R.id.workout_widget_text, workoutText.toString().trim());
        } else {
            views.setTextViewText(R.id.workout_widget_text, context.getString(R.string.no_workouts_today));
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
} 