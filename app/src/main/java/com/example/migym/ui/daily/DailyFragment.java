package com.example.migym.ui.daily;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.migym.R;
import com.example.migym.databinding.FragmentDailyBinding;
import com.example.migym.models.Workout;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DailyFragment extends Fragment {
    private FragmentDailyBinding binding;
    private DailyWorkoutAdapter workoutAdapter;
    private final Map<String, List<Workout>> dayWorkouts = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDailyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTabLayout();
        setupRecyclerView();
        loadWorkouts();
    }

    private void setupTabLayout() {
        if (binding == null || !isAdded()) return;

        String[] days = getResources().getStringArray(R.array.days_of_week);
        for (String day : days) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(day));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null && tab.getText() != null) {
                    updateWorkoutList(tab.getText().toString());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No implementation needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No implementation needed
            }
        });

        // Select current day by default
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int tabIndex = (currentDay + 5) % 7; // Convert Sunday=1 to Monday=0
        TabLayout.Tab tab = binding.tabLayout.getTabAt(tabIndex);
        if (tab != null) {
            tab.select();
        }
    }

    private void setupRecyclerView() {
        if (binding == null || !isAdded()) return;

        workoutAdapter = new DailyWorkoutAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(workoutAdapter);
    }

    private void loadWorkouts() {
        if (binding == null || !isAdded()) return;

        // TODO: Load workouts from database
        // For now, we'll use dummy data
        List<Workout> dummyWorkouts = new ArrayList<>();
        dummyWorkouts.add(new Workout("Morning Run", "Cardio", 2, "08:00")); // Monday
        dummyWorkouts.add(new Workout("Weight Training", "Strength", 2, "10:00")); // Monday
        dummyWorkouts.add(new Workout("Yoga", "Flexibility", 3, "09:00")); // Tuesday
        // Add more dummy workouts as needed

        // Group workouts by day
        for (Workout workout : dummyWorkouts) {
            String day = getDayName(workout.getDayOfWeek());
            dayWorkouts.computeIfAbsent(day, k -> new ArrayList<>()).add(workout);
        }

        // Update the list for the current tab
        TabLayout.Tab selectedTab = binding.tabLayout.getTabAt(binding.tabLayout.getSelectedTabPosition());
        if (selectedTab != null && selectedTab.getText() != null) {
            updateWorkoutList(selectedTab.getText().toString());
        }
    }

    private String getDayName(int dayOfWeek) {
        if (!isAdded()) return "";

        String[] days = getResources().getStringArray(R.array.days_of_week);
        // Convert Calendar.DAY_OF_WEEK (Sunday=1) to our array index (Monday=0)
        int index = (dayOfWeek + 5) % 7;
        return (index >= 0 && index < days.length) ? days[index] : "";
    }

    private void updateWorkoutList(String day) {
        if (binding == null || !isAdded() || day == null) return;

        List<Workout> workouts = dayWorkouts.getOrDefault(day, new ArrayList<>());
        workoutAdapter.submitList(new ArrayList<>(workouts));
        
        // Show/hide empty state
        if (workouts.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.emptyStateText.setText(getString(R.string.no_workouts_for_day, day));
        } else {
            binding.emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 