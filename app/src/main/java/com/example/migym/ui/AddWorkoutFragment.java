package com.example.migym.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.migym.R;
import com.example.migym.dialogs.AddWorkoutDialog;
import com.example.migym.models.Workout;
import com.example.migym.viewmodels.WorkoutViewModel;

import java.util.List;

public class AddWorkoutFragment extends Fragment implements AddWorkoutDialog.OnWorkoutListener, WorkoutViewModel.OnWorkoutAddListener {

    private WorkoutViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Show the AddWorkoutDialog when the fragment is created
        AddWorkoutDialog dialog = AddWorkoutDialog.newInstance(null);
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "AddWorkoutDialog");
    }

    @Override
    public void onWorkoutCreated(Workout workout) {
        // Use the ViewModel to add the workout
        viewModel.addWorkout(workout, this);
    }

    @Override
    public void onWorkoutUpdated(Workout workout) {
        // Use the ViewModel to update the workout
        viewModel.updateWorkout(workout);
        // Navigate back to the workout list
        requireActivity().onBackPressed();
    }

    @Override
    public void onWorkoutAdded() {
        // Show success message
        Toast.makeText(requireContext(), R.string.workout_added, Toast.LENGTH_SHORT).show();
        // Navigate back to the workout list
        requireActivity().onBackPressed();
    }

    @Override
    public void onError(String error) {
        // Show error message
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConflict(List<Workout> conflicts) {
        // Show conflict dialog
        if (conflicts != null && !conflicts.isEmpty()) {
            Toast.makeText(requireContext(), R.string.workout_conflict, Toast.LENGTH_LONG).show();
        }
    }
} 