package com.example.migym.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;
import com.example.migym.R;
import com.example.migym.models.Workout;
import com.example.migym.databinding.FragmentWorkoutDetailBinding;
import com.example.migym.viewmodels.WorkoutViewModel;

public class WorkoutDetailFragment extends Fragment {
    private FragmentWorkoutDetailBinding binding;
    private WorkoutViewModel workoutViewModel;
    private Workout currentWorkout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        
        if (getArguments() != null) {
            String workoutId = getArguments().getString("workoutId");
            workoutViewModel.getWorkoutById(workoutId).observe(getViewLifecycleOwner(), workout -> {
                if (workout != null) {
                    currentWorkout = workout;
                    updateUI(workout);
                }
            });
        }

        binding.saveButton.setOnClickListener(v -> {
            if (currentWorkout != null) {
                saveWorkout();
            }
        });

        binding.deleteButton.setOnClickListener(v -> {
            if (currentWorkout != null) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void updateUI(Workout workout) {
        String[] days = getResources().getStringArray(R.array.days_of_week);
        String[] types = getResources().getStringArray(R.array.workout_types);

        binding.titleEditText.setText(workout.getTitle());
        binding.descriptionEditText.setText(workout.getDescription());
        binding.locationEditText.setText(workout.getLocation());
        binding.daySpinner.setSelection(workout.getDayOfWeek() - 1);
        binding.typeSpinner.setSelection(workout.getTypeAsInt());
        binding.timeEditText.setText(workout.getTime());
    }

    private void saveWorkout() {
        String title = binding.titleEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String location = binding.locationEditText.getText().toString().trim();
        int dayOfWeek = binding.daySpinner.getSelectedItemPosition() + 1;
        int type = binding.typeSpinner.getSelectedItemPosition();
        String time = binding.timeEditText.getText().toString();

        currentWorkout.setTitle(title);
        currentWorkout.setDescription(description);
        currentWorkout.setLocation(location);
        currentWorkout.setDayOfWeek(dayOfWeek);
        currentWorkout.setTypeFromInt(type);
        currentWorkout.setTime(time);

        workoutViewModel.updateWorkout(currentWorkout);
        Snackbar.make(binding.getRoot(), R.string.workout_updated, Snackbar.LENGTH_SHORT).show();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.navigateUp();
    }

    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_workout)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    workoutViewModel.deleteWorkout(currentWorkout);
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.navigateUp();
                    Snackbar.make(binding.getRoot(), R.string.workout_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workout_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 