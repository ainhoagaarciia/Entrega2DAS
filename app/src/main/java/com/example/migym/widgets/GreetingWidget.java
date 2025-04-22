package com.example.migym.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.migym.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GreetingWidget extends AppWidgetProvider {
    private static final String ACTION_UPDATE_WIDGET = "com.example.migym.ACTION_UPDATE_WIDGET";
    private static final int UPDATE_INTERVAL = 1000 * 60 * 30; // 30 minutos

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new android.content.ComponentName(context, GreetingWidget.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_greeting);

        // Obtener la hora actual
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = calendar.get(Calendar.MINUTE);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = timeFormat.format(calendar.getTime());

        // Determinar el saludo según la hora
        String greeting = "";
        String message = "";
        
        // Solo mostrar el saludo a las 9:00 AM
        if (hourOfDay == 9 && minuteOfHour == 0) {
            greeting = context.getString(R.string.good_morning);
            message = "¡Es hora de comenzar tu día con energía!";
        } else {
            // Si no es la hora especificada, ocultar el widget
            views.setViewVisibility(R.id.widget_greeting, android.view.View.GONE);
            views.setViewVisibility(R.id.widget_message, android.view.View.GONE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        // Actualizar las vistas
        views.setViewVisibility(R.id.widget_greeting, android.view.View.VISIBLE);
        views.setViewVisibility(R.id.widget_message, android.view.View.VISIBLE);
        views.setTextViewText(R.id.widget_greeting, greeting);
        views.setTextViewText(R.id.widget_message, message);

        // Configurar la actualización periódica
        Intent updateIntent = new Intent(context, GreetingWidget.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(
            context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_greeting, updatePendingIntent);

        // Actualizar el widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
} 