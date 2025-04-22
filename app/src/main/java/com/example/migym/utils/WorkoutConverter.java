package com.example.migym.utils;

import com.example.migym.models.Workout;
import com.example.migym.models.WorkoutSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkoutConverter {
    private static final String[] days = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    public static Workout toWorkout(WorkoutSession session) {
        if (session == null) return null;

        Workout workout = new Workout();
        workout.setId(UUID.randomUUID().toString());
        workout.setName(session.getName());
        workout.setDescription(session.getDescription());
        workout.setTypeFromInt(Integer.parseInt(session.getType()));
        workout.setDayOfWeek(session.getDayOfWeek());
        workout.setTime(session.getTime());
        workout.setDuration(session.getDuration());
        workout.setInstructor(session.getInstructor());
        workout.setLocation(session.getLocation());
        workout.setCompleted(session.isCompleted() ? 1 : 0);
        return workout;
    }

    public static WorkoutSession toWorkoutSession(Workout workout) {
        if (workout == null) return null;

        WorkoutSession session = new WorkoutSession();
        session.setName(workout.getName());
        session.setDescription(workout.getDescription());
        session.setType(String.valueOf(workout.getType()));
        session.setDayOfWeek(workout.getDayOfWeek());
        session.setTime(workout.getTime());
        session.setDuration(workout.getDuration());
        session.setInstructor(workout.getInstructor());
        session.setLocation(workout.getLocation());
        session.setCompleted(workout.getCompleted() == 1);
        return session;
    }

    public static String getDayName(int dayOfWeek) {
        if (dayOfWeek >= 0 && dayOfWeek < days.length) {
            return days[dayOfWeek];
        }
        return "Desconocido";
    }
    
    public static List<Workout> toWorkouts(List<WorkoutSession> sessions) {
        List<Workout> workouts = new ArrayList<>();
        for (WorkoutSession session : sessions) {
            workouts.add(toWorkout(session));
        }
        return workouts;
    }
    
    public static List<WorkoutSession> toWorkoutSessions(List<Workout> workouts) {
        List<WorkoutSession> sessions = new ArrayList<>();
        for (Workout workout : workouts) {
            sessions.add(toWorkoutSession(workout));
        }
        return sessions;
    }
}