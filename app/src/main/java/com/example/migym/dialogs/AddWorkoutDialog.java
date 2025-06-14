package com.example.migym.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.example.migym.R;
import com.example.migym.models.Workout;
import com.example.migym.databinding.DialogAddWorkoutBinding;
import com.example.migym.viewmodels.WorkoutViewModel;
import com.example.migym.ui.MapActivity;
import com.example.migym.utils.WorkoutAlarmManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddWorkoutDialog extends DialogFragment {
    private static final String TAG = "AddWorkoutDialog";
    private DialogAddWorkoutBinding binding;
    private OnWorkoutListener listener;
    private Workout existingWorkout;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private WorkoutViewModel viewModel;

    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                try {
                    double latitude = result.getData().getDoubleExtra("latitude", 0.0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0.0);
                    String address = result.getData().getStringExtra("address");

                    if (latitude != 0.0 && longitude != 0.0) {
                        selectedLatitude = latitude;
                        selectedLongitude = longitude;
                        
                        if (address != null && !address.isEmpty()) {
                            binding.locationEditText.setText(address);
                        } else {
                            binding.locationEditText.setText(
                                String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude));
                        }
                    } else {
                        Toast.makeText(requireContext(), 
                            getString(R.string.invalid_location), 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing map result", e);
                    Toast.makeText(requireContext(), 
                        getString(R.string.error_getting_location), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

    public interface OnWorkoutListener {
        void onWorkoutCreated(Workout workout);
        void onWorkoutUpdated(Workout workout);
    }

    public static AddWorkoutDialog newInstance(@Nullable Workout workout) {
        AddWorkoutDialog dialog = new AddWorkoutDialog();
        if (workout != null) {
            dialog.existingWorkout = workout;
        }
        return dialog;
    }

    public void setListener(OnWorkoutListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddWorkoutBinding.inflate(LayoutInflater.from(getContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        setupLocationButton();
        
        if (existingWorkout != null) {
            loadWorkoutData();
        } else {
            // Rellenar duración por defecto si existe
            int defaultDuration = 0;
            try {
                defaultDuration = Integer.parseInt(androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString("pref_default_duration", "0"));
            } catch (Exception ignored) {}
            if (defaultDuration > 0) {
                binding.durationEditText.setText(String.valueOf(defaultDuration));
            }
        }

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
            .setTitle(existingWorkout != null ? R.string.edit_workout : R.string.add_workout)
            .setView(binding.getRoot())
            .setPositiveButton(R.string.save, (dialog1, which) -> saveWorkout())
            .setNegativeButton(R.string.cancel, null)
            .create();

        // Setup time button after dialog creation
        setupTimeButton();
        
        return dialog;
    }

    private void setupLocationButton() {
        binding.selectLocationButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), MapActivity.class);
                intent.putExtra("action", "select_location");
                mapLauncher.launch(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error launching map activity", e);
                Toast.makeText(requireContext(), 
                    getString(R.string.error_opening_map), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTimeButton() {
        // Configurar spinner de horas (0-23)
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format(Locale.getDefault(), "%02d", i);
        }
        android.widget.ArrayAdapter<String> hourAdapter = new android.widget.ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.hourSpinner.setAdapter(hourAdapter);

        // Configurar spinner de minutos (0-59)
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format(Locale.getDefault(), "%02d", i);
        }
        android.widget.ArrayAdapter<String> minuteAdapter = new android.widget.ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.minuteSpinner.setAdapter(minuteAdapter);

        // Establecer valores por defecto
        if (existingWorkout == null) {
            Calendar calendar = Calendar.getInstance();
            binding.hourSpinner.setSelection(calendar.get(Calendar.HOUR_OF_DAY));
            binding.minuteSpinner.setSelection(calendar.get(Calendar.MINUTE));
        }
    }

    private void loadWorkoutData() {
        binding.nameEditText.setText(existingWorkout.getName());
        binding.descriptionEditText.setText(existingWorkout.getDescription());
        binding.locationEditText.setText(existingWorkout.getLocation());
        
        // Cargar hora existente
        String[] timeParts = existingWorkout.getTime().split(":");
        if (timeParts.length == 2) {
            try {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                binding.hourSpinner.setSelection(hour);
                binding.minuteSpinner.setSelection(minute);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing time", e);
            }
        }
        
        binding.durationEditText.setText(String.valueOf(existingWorkout.getDuration()));
        selectedLatitude = existingWorkout.getLatitude();
        selectedLongitude = existingWorkout.getLongitude();
        // Seleccionar el día de la semana en el spinner
        binding.daySpinner.setSelection(existingWorkout.getDayOfWeek());
    }

    private void saveWorkout() {
        String name = binding.nameEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String location = binding.locationEditText.getText().toString().trim();
        String time = String.format(Locale.getDefault(), "%02d:%02d",
            binding.hourSpinner.getSelectedItemPosition(),
            binding.minuteSpinner.getSelectedItemPosition());
        String durationStr = binding.durationEditText.getText().toString().trim();
        int dayOfWeek = binding.daySpinner.getSelectedItemPosition();

        // Aplicar duración predeterminada si está vacío
        if (TextUtils.isEmpty(durationStr)) {
            try {
                int defaultDuration = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString("pref_default_duration", "0"));
                if (defaultDuration > 0) {
                    durationStr = String.valueOf(defaultDuration);
                    binding.durationEditText.setText(durationStr);
                }
            } catch (Exception ignored) {}
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), R.string.error_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            Toast.makeText(requireContext(), R.string.error_location_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar duración después de aplicar el valor predeterminado
        if (TextUtils.isEmpty(durationStr)) {
            Toast.makeText(requireContext(), R.string.error_duration_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                Toast.makeText(requireContext(), R.string.error_duration_positive, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.error_duration_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        Workout workout;
        if (existingWorkout != null) {
            workout = existingWorkout;
            workout.setName(name);
            workout.setTitle(name);
            workout.setDescription(description);
            workout.setLocation(location);
            workout.setTime(time);
            workout.setDuration(duration);
            workout.setLatitude(selectedLatitude);
            workout.setLongitude(selectedLongitude);
            workout.setDayOfWeek(dayOfWeek);
            // Programar/cancelar alarma según preferencias
            handleWorkoutAlarm(workout);
            listener.onWorkoutUpdated(workout);
        } else {
            workout = new Workout();
            workout.setId(UUID.randomUUID().toString());
            workout.setName(name);
            workout.setTitle(name);
            workout.setDescription(description);
            workout.setLocation(location);
            workout.setTime(time);
            workout.setDuration(duration);
            workout.setLatitude(selectedLatitude);
            workout.setLongitude(selectedLongitude);
            workout.setDayOfWeek(dayOfWeek);
            workout.setType("OTHER");
            workout.setCompleted(0);
            // Programar/cancelar alarma según preferencias
            handleWorkoutAlarm(workout);
            listener.onWorkoutCreated(workout);
        }
    }

    private void handleWorkoutAlarm(Workout workout) {
        // Obtener preferencias
        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean("enable_notifications", true);
        int minutesBefore = 30;
        try {
            minutesBefore = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("notification_time", "30"));
        } catch (Exception ignored) {}
        if (notificationsEnabled) {
            WorkoutAlarmManager.scheduleWorkoutAlarm(requireContext(), workout, minutesBefore);
        } else {
            WorkoutAlarmManager.cancelWorkoutAlarm(requireContext(), workout);
        }
    }

    private void showConflictDialog(Workout newWorkout, List<Workout> conflicts) {
        StringBuilder message = new StringBuilder(getString(R.string.workout_time_conflict));
        for (Workout conflict : conflicts) {
            message.append("\n- ").append(conflict.getName());
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.time_conflict)
            .setMessage(message)
            .setPositiveButton(R.string.force_add, (dialog, which) -> {
                viewModel.addWorkout(newWorkout, new WorkoutViewModel.OnWorkoutAddListener() {
                    @Override
                    public void onWorkoutAdded() {
                        if (listener != null) {
                            listener.onWorkoutCreated(newWorkout);
                        }
                        dismiss();
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.workout_saved, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onConflict(List<Workout> conflicts) {
                        // Ignoramos los conflictos ya que el usuario ha decidido forzar la adición
                    }
                });
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