package com.example.migym.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final String[] DAYS_OF_WEEK = {
        "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    };

    public static String getFormattedTime(Date date) {
        return TIME_FORMAT.format(date);
    }

    public static String getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return DAYS_OF_WEEK[dayOfWeek - 1];
    }

    public static String getDayName(int dayOfWeek) {
        // Calendar.DAY_OF_WEEK starts with Sunday (1), so we need to adjust
        int index = (dayOfWeek + 6) % 7;
        return DAYS_OF_WEEK[index];
    }

    public static String getFormattedTime(String time) {
        return time;
    }
} 