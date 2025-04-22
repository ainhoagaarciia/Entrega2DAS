package com.example.migym.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.migym.R;
import com.example.migym.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.registerButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

            if (!validateForm(email, password, confirmPassword)) {
                return;
            }

            showProgress(true);
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.registration_failed, task.getException().getMessage()),
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });

        binding.loginLink.setOnClickListener(v -> finish());
    }

    private boolean validateForm(String email, String password, String confirmPassword) {
        boolean valid = true;

        if (email.isEmpty()) {
            binding.emailInput.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            binding.emailInput.setError(null);
        }

        if (password.isEmpty()) {
            binding.passwordInput.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (password.length() < 6) {
            binding.passwordInput.setError(getString(R.string.error_password_too_short));
            valid = false;
        } else {
            binding.passwordInput.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInput.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            binding.confirmPasswordInput.setError(getString(R.string.error_passwords_dont_match));
            valid = false;
        } else {
            binding.confirmPasswordInput.setError(null);
        }

        return valid;
    }

    private void showProgress(boolean show) {
        binding.registerButton.setEnabled(!show);
        binding.emailInput.setEnabled(!show);
        binding.passwordInput.setEnabled(!show);
        binding.confirmPasswordInput.setEnabled(!show);
        binding.loginLink.setEnabled(!show);
    }
} 