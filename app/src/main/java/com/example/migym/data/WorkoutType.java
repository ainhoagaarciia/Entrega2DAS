package com.example.migym.data;

public enum WorkoutType {
    CARDIO,
    STRENGTH,
    FLEXIBILITY,
    HIIT,
    YOGA,
    OTHER;

    public static String[] getNames() {
        WorkoutType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].toString();
        }
        return names;
    }

    public static WorkoutType fromString(String text) {
        for (WorkoutType type : WorkoutType.values()) {
            if (type.toString().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return OTHER;
    }

    @Override
    public String toString() {
        switch (this) {
            case CARDIO:
                return "Cardio";
            case STRENGTH:
                return "Strength";
            case FLEXIBILITY:
                return "Flexibility";
            case HIIT:
                return "HIIT";
            default:
                return "Other";
        }
    }
} 