package com.example.migym.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.migym.MainActivity;
import com.example.migym.R;
import com.example.migym.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Patterns;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is already authenticated
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User already authenticated, redirecting to MainActivity");
            startMainActivity();
            return;
        }

        // Setup authentication buttons
        setupLoginButton();
        setupGoogleSignIn();
        setupRegisterButton();
    }

    private void setupLoginButton() {
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (!validateInput(email, password)) {
                return;
            }

            showLoading();

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "User signed in: " + user.getEmail());
                            startMainActivity();
                        } else {
                            Log.e(TAG, "User is null after successful sign in");
                            showError("Error al iniciar sesión");
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        String errorMessage = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        showError(errorMessage);
                    }
                    hideLoading();
                });
        });
    }

    private void setupRegisterButton() {
        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupGoogleSignIn() {
        binding.googleSignInButton.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-In not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

    private void startMainActivity() {
        Log.d(TAG, "Starting MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.fill_all_fields));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return false;
        }
        if (password.length() < 6) {
            showError(getString(R.string.error_password_too_short));
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.loginButton.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 