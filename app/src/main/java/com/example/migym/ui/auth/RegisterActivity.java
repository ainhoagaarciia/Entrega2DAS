package com.example.migym.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.migym.MainActivity;
import com.example.migym.R;
import com.example.migym.databinding.ActivityRegisterBinding;
import com.example.migym.utils.UserPreferences;
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

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, R.string.error_passwords_dont_match, Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostrar progreso
            showProgress(true);

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Guardar datos del usuario
                            UserPreferences userPreferences = new UserPreferences(this);
                            userPreferences.setLoggedIn(true);
                            userPreferences.saveEmail(email);
                            
                            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                            
                            // Redirigir al MainActivity
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : 
                            getString(R.string.registration_failed);
                        Toast.makeText(this, getString(R.string.registration_failed, errorMessage),
                            Toast.LENGTH_LONG).show();
                    }
                });
        });

        binding.loginLink.setOnClickListener(v -> {
            // Volver al LoginActivity
            finish();
        });
    }

    private void showProgress(boolean show) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        // Volver al LoginActivity
        finish();
    }
} 