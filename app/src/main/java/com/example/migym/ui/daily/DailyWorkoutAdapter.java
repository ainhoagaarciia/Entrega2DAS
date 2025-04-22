package com.example.migym.ui.daily;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.migym.R;
import com.example.migym.databinding.ItemDailyWorkoutBinding;
import com.example.migym.models.Workout;

public class DailyWorkoutAdapter extends ListAdapter<Workout, DailyWorkoutAdapter.WorkoutViewHolder> {

    public DailyWorkoutAdapter() {
        super(new WorkoutDiffCallback());
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDailyWorkoutBinding binding = ItemDailyWorkoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new WorkoutViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final ItemDailyWorkoutBinding binding;

        public WorkoutViewHolder(ItemDailyWorkoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Workout workout) {
            binding.workoutTitle.setText(workout.getTitle());
            binding.workoutType.setText(String.valueOf(workout.getType()));
            binding.workoutTime.setText(workout.getTime());
            binding.workoutDuration.setText(binding.getRoot().getContext().getString(
                    R.string.duration_minutes,
                    workout.getDuration()
            ));
        }
    }

    private static class WorkoutDiffCallback extends DiffUtil.ItemCallback<Workout> {
        @Override
        public boolean areItemsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.getType() == newItem.getType() &&
                   oldItem.getTime().equals(newItem.getTime()) &&
                   oldItem.getLocation().equals(newItem.getLocation());
        }
    }
} 