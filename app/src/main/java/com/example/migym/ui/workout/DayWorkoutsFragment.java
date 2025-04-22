package com.example.migym.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.migym.R;
import com.example.migym.adapters.WorkoutAdapter;
import com.example.migym.models.Workout;
import com.example.migym.databinding.FragmentDayWorkoutsBinding;
import com.example.migym.viewmodels.WorkoutViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

public class DayWorkoutsFragment extends Fragment implements WorkoutAdapter.OnWorkoutClickListener {
    private static final String ARG_DAY = "day";
    private FragmentDayWorkoutsBinding binding;
    private WorkoutViewModel workoutViewModel;
    private WorkoutAdapter workoutAdapter;
    private int day;

    public static DayWorkoutsFragment newInstance(String dayString) {
        DayWorkoutsFragment fragment = new DayWorkoutsFragment();
        Bundle args = new Bundle();
        int day = Integer.parseInt(dayString);
        args.putInt(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            day = getArguments().getInt(ARG_DAY, 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDayWorkoutsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        setupRecyclerView();
        observeWorkouts();
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter(this);
        binding.workoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workoutRecyclerView.setAdapter(workoutAdapter);
    }

    private void observeWorkouts() {
        workoutViewModel.getWorkoutsByDay(day).observe(getViewLifecycleOwner(), workouts -> {
            if (workouts != null && !workouts.isEmpty()) {
                workoutAdapter.submitList(workouts);
                binding.emptyView.setVisibility(View.GONE);
            } else {
                workoutAdapter.submitList(new ArrayList<>());
                binding.emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onWorkoutClick(Workout workout) {
        Bundle args = new Bundle();
        args.putString("workoutId", workout.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_dayWorkouts_to_workoutDetail, args);
    }

    @Override
    public void onDeleteClick(Workout workout) {
        workoutViewModel.deleteWorkout(workout);
        Snackbar.make(requireView(), R.string.workout_deleted, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 