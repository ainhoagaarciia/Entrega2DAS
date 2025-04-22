package com.example.migym.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.migym.R;
import com.example.migym.databinding.ActivityProfileBinding;
import com.example.migym.viewmodels.UserViewModel;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                binding.editTextName.setText(user.getName());
                binding.editTextEmail.setText(user.getEmail());
            }
        });

        userViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void setupListeners() {
        binding.buttonSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.editTextName.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            showError(getString(R.string.profile_fill_required));
            return;
        }

        userViewModel.updateProfile(name, email);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 