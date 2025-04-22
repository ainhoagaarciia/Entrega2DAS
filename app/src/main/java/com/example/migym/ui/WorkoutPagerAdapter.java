package com.example.migym.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.migym.ui.workout.DayWorkoutsFragment;

public class WorkoutPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 7;

    public WorkoutPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return DayWorkoutsFragment.newInstance(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
} 