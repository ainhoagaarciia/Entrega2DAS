package com.example.migym.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.migym.R;
import com.example.migym.adapters.WorkoutAdapter;
import com.example.migym.databinding.FragmentWorkoutListBinding;
import com.example.migym.dialogs.AddWorkoutDialog;
import com.example.migym.models.Workout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class WorkoutListFragment extends Fragment implements WorkoutAdapter.OnWorkoutClickListener {
    private FragmentWorkoutListBinding binding;
    private WorkoutViewModel viewModel;
    private WorkoutAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        setupRecyclerView();
        setupFab();
        observeWorkouts();
    }

    private void setupRecyclerView() {
        adapter = new WorkoutAdapter(this);
        binding.workoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workoutRecyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        binding.addWorkoutFab.setOnClickListener(v -> showAddWorkoutDialog());
    }

    private void observeWorkouts() {
        viewModel.getAllWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            adapter.submitList(workouts);
            binding.emptyView.setVisibility(workouts.isEmpty() ? View.VISIBLE : View.GONE);
            binding.workoutRecyclerView.setVisibility(workouts.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void showAddWorkoutDialog() {
        AddWorkoutDialog dialog = AddWorkoutDialog.newInstance(null);
        dialog.setListener(new AddWorkoutDialog.OnWorkoutListener() {
            @Override
            public void onWorkoutCreated(Workout workout) {
                viewModel.addWorkout(workout, new WorkoutViewModel.OnWorkoutAddListener() {
                    @Override
                    public void onWorkoutAdded() {
                        // La lista se actualizará automáticamente a través del LiveData
                    }

                    @Override
                    public void onError(String error) {
                        // Mostrar error al usuario
                    }

                    @Override
                    public void onConflict(List<Workout> conflicts) {
                        // Mostrar diálogo de conflicto
                        showConflictDialog(workout, conflicts);
                    }
                });
            }

            @Override
            public void onWorkoutUpdated(Workout workout) {
                viewModel.updateWorkout(workout);
            }
        });
        dialog.show(getChildFragmentManager(), "addWorkout");
    }

    private void showConflictDialog(Workout newWorkout, List<Workout> conflicts) {
        StringBuilder message = new StringBuilder(getString(R.string.workout_time_conflict));
        for (Workout conflict : conflicts) {
            message.append("\n- ").append(conflict.getTitle());
        }
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.workout_conflict)
            .setMessage(message)
            .setPositiveButton(R.string.force_add, (dialog, which) -> {
                // Forzar la adición del workout
                viewModel.addWorkout(newWorkout, new WorkoutViewModel.OnWorkoutAddListener() {
                    @Override
                    public void onWorkoutAdded() {
                        // La lista se actualizará automáticamente
                    }

                    @Override
                    public void onError(String error) {
                        // Mostrar error al usuario
                    }

                    @Override
                    public void onConflict(List<Workout> conflicts) {
                        // No debería ocurrir ya que estamos forzando la adición
                    }
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onWorkoutClick(Workout workout) {
        AddWorkoutDialog dialog = AddWorkoutDialog.newInstance(workout);
        dialog.setListener(new AddWorkoutDialog.OnWorkoutListener() {
            @Override
            public void onWorkoutCreated(Workout newWorkout) {
                // No se usa aquí
            }
            @Override
            public void onWorkoutUpdated(Workout updatedWorkout) {
                viewModel.updateWorkout(updatedWorkout);
            }
        });
        dialog.show(getChildFragmentManager(), "editWorkout");
    }

    @Override
    public void onDeleteClick(Workout workout) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_workout)
            .setMessage(R.string.delete_workout_message)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                viewModel.deleteWorkout(workout);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 