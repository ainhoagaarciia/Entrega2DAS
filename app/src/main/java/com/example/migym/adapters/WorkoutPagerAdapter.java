package com.example.migym.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.migym.ui.workout.DayWorkoutsFragment;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPagerAdapter extends FragmentStateAdapter {
    private final List<String> days;

    public WorkoutPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.days = new ArrayList<>();
        initializeDays();
    }

    private void initializeDays() {
        days.add("Domingo");
        days.add("Lunes");
        days.add("Martes");
        days.add("Miércoles");
        days.add("Jueves");
        days.add("Viernes");
        days.add("Sábado");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return DayWorkoutsFragment.newInstance(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public String getDayAt(int position) {
        return days.get(position);
    }
} 