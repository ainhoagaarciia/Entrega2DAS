package com.example.migym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.migym.R;
import com.example.migym.models.Workout;
import java.util.Locale;

public class WorkoutAdapter extends ListAdapter<Workout, WorkoutAdapter.WorkoutViewHolder> {
    private final OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onDeleteClick(Workout workout);
    }

    public WorkoutAdapter(OnWorkoutClickListener listener) {
        super(new DiffUtil.ItemCallback<Workout>() {
            @Override
            public boolean areItemsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView timeText;
        private final TextView locationText;
        private final TextView durationText;
        private final ImageButton deleteButton;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.workoutNameText);
            timeText = itemView.findViewById(R.id.workoutTimeText);
            locationText = itemView.findViewById(R.id.workoutLocationText);
            durationText = itemView.findViewById(R.id.workoutDurationText);
            deleteButton = itemView.findViewById(R.id.deleteWorkoutButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onWorkoutClick(getItem(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }

        void bind(Workout workout) {
            nameText.setText(workout.getName());
            timeText.setText(workout.getTime());
            locationText.setText(workout.getLocation());
            durationText.setText(String.format(Locale.getDefault(), "%d min", workout.getDuration()));
        }
    }
}