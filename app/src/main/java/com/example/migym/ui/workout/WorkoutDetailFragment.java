package com.example.migym.ui.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.migym.R;
import com.example.migym.databinding.FragmentWorkoutDetailBinding;
import com.example.migym.models.Workout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.UUID;
import java.util.List;

public class WorkoutDetailFragment extends Fragment {
    private FragmentWorkoutDetailBinding binding;
    private WorkoutViewModel workoutViewModel;
    private String workoutId;
    private Workout currentWorkout;
    private boolean isNewWorkout = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            setupViewModel();
            
            if (getArguments() != null) {
                workoutId = getArguments().getString("workoutId");
                if (workoutId != null) {
                    loadWorkoutDetails();
                } else {
                    // Si no hay workoutId, estamos creando un nuevo workout
                    isNewWorkout = true;
                    setupNewWorkout();
                }
            } else {
                // Si no hay argumentos, estamos creando un nuevo workout
                isNewWorkout = true;
                setupNewWorkout();
            }
        } catch (Exception e) {
            Log.e("WorkoutDetailFragment", "Error in onViewCreated", e);
            if (isAdded()) {
                Snackbar.make(requireView(), "Error al cargar el formulario: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void setupViewModel() {
        try {
            workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        } catch (Exception e) {
            Log.e("WorkoutDetailFragment", "Error setting up ViewModel", e);
            if (isAdded()) {
                Snackbar.make(requireView(), "Error al inicializar el modelo de datos", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void setupNewWorkout() {
        if (binding == null) return;
        
        try {
            // Ocultar el botón de eliminar para nuevos workouts
            binding.deleteButton.setVisibility(View.GONE);
            
            // Configurar el botón de guardar para crear un nuevo workout
            binding.saveButton.setOnClickListener(v -> {
                createNewWorkout();
            });
        } catch (Exception e) {
            Log.e("WorkoutDetailFragment", "Error in setupNewWorkout", e);
            if (isAdded()) {
                Snackbar.make(requireView(), "Error al configurar el formulario", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void loadWorkoutDetails() {
        workoutViewModel.getWorkoutById(workoutId).observe(getViewLifecycleOwner(), workout -> {
            if (workout != null) {
                currentWorkout = workout;
                updateUI(workout);
            }
        });
    }

    private void updateUI(Workout workout) {
        if (workout != null) {
            binding.titleEditText.setText(workout.getTitle());
            binding.descriptionEditText.setText(workout.getDescription());
            binding.locationEditText.setText(workout.getLocation());
            binding.daySpinner.setSelection(workout.getDayOfWeek() - 1);
            binding.typeSpinner.setSelection(workout.getTypeAsInt());
            binding.timeEditText.setText(workout.getTime());
            
            binding.deleteButton.setOnClickListener(v -> {
                workoutViewModel.deleteWorkout(workout);
                Snackbar.make(requireView(), R.string.workout_deleted, Snackbar.LENGTH_LONG).show();
                requireActivity().onBackPressed();
            });

            binding.saveButton.setOnClickListener(v -> {
                updateWorkout(workout);
            });
        }
    }

    private void createNewWorkout() {
        if (binding == null || workoutViewModel == null) {
            Log.e("WorkoutDetailFragment", "Binding or ViewModel is null");
            return;
        }
        
        try {
            // Verificar que los campos de texto no sean nulos
            if (binding.titleEditText == null || binding.descriptionEditText == null || 
                binding.locationEditText == null || binding.timeEditText == null ||
                binding.daySpinner == null || binding.typeSpinner == null) {
                Log.e("WorkoutDetailFragment", "One or more UI components are null");
                Snackbar.make(requireView(), "Error: Componentes de la interfaz no inicializados", Snackbar.LENGTH_LONG).show();
                return;
            }
            
            String title = binding.titleEditText.getText() != null ? 
                binding.titleEditText.getText().toString().trim() : "";
            String description = binding.descriptionEditText.getText() != null ? 
                binding.descriptionEditText.getText().toString().trim() : "";
            String location = binding.locationEditText.getText() != null ? 
                binding.locationEditText.getText().toString().trim() : "";
            String time = binding.timeEditText.getText() != null ? 
                binding.timeEditText.getText().toString().trim() : "";
            
            // Validar campos requeridos
            if (title.isEmpty()) {
                binding.titleEditText.setError(getString(R.string.error_title_required));
                return;
            }
            
            if (time.isEmpty()) {
                binding.timeEditText.setError(getString(R.string.error_time_required));
                return;
            }
            
            // Crear nuevo workout
            Workout newWorkout = new Workout();
            newWorkout.setId(UUID.randomUUID().toString());
            newWorkout.setTitle(title);
            newWorkout.setName(title);
            newWorkout.setDescription(description);
            newWorkout.setLocation(location);
            newWorkout.setTime(time);
            
            // Verificar que los spinners estén inicializados
            int dayPosition = binding.daySpinner.getSelectedItemPosition();
            int typePosition = binding.typeSpinner.getSelectedItemPosition();
            
            newWorkout.setDayOfWeek(dayPosition + 1);
            newWorkout.setTypeFromInt(typePosition);
            newWorkout.setDuration(30); // Duración predeterminada
            newWorkout.setCompleted(0);
            
            // Guardar el workout
            workoutViewModel.addWorkout(newWorkout, new WorkoutViewModel.OnWorkoutAddListener() {
                @Override
                public void onWorkoutAdded() {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Snackbar.make(requireView(), R.string.workout_added, Snackbar.LENGTH_LONG).show();
                            requireActivity().onBackPressed();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onConflict(List<Workout> conflicts) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            showConflictDialog(conflicts, newWorkout);
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e("WorkoutDetailFragment", "Error creating workout", e);
            if (isAdded()) {
                Snackbar.make(requireView(), "Error al crear el entrenamiento: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void showConflictDialog(List<Workout> conflicts, Workout newWorkout) {
        StringBuilder message = new StringBuilder(getString(R.string.workout_time_conflict));
        for (Workout conflict : conflicts) {
            message.append("\n- ").append(conflict.getTitle()).append(" (").append(conflict.getTime()).append(")");
        }
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.time_conflict)
            .setMessage(message.toString())
            .setPositiveButton(R.string.force_add, (dialog, which) -> {
                workoutViewModel.forceAddWorkout(newWorkout);
                Snackbar.make(requireView(), R.string.workout_added, Snackbar.LENGTH_LONG).show();
                requireActivity().onBackPressed();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void updateWorkout(Workout workout) {
        workout.setTitle(binding.titleEditText.getText().toString());
        workout.setDescription(binding.descriptionEditText.getText().toString());
        workout.setLocation(binding.locationEditText.getText().toString());
        workout.setTime(binding.timeEditText.getText().toString());
        workout.setDayOfWeek(binding.daySpinner.getSelectedItemPosition() + 1);
        workout.setTypeFromInt(binding.typeSpinner.getSelectedItemPosition());

        workoutViewModel.updateWorkout(workout);
        Snackbar.make(requireView(), R.string.workout_saved, Snackbar.LENGTH_LONG).show();
        requireActivity().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 