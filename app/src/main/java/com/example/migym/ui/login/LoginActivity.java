package com.example.migym.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.migym.MainActivity;
import com.example.migym.R;
import com.example.migym.databinding.ActivityLoginBinding;
import com.example.migym.ui.login.RegisterActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.example.migym.MiGymApplication;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up click listeners
        binding.loginButton.setOnClickListener(v -> handleEmailPasswordLogin());
        binding.googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        binding.registerButton.setOnClickListener(v -> navigateToRegister());
    }

    private void handleEmailPasswordLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (!validateLoginInput(email, password)) {
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private boolean validateLoginInput(String email, String password) {
        if (email.isEmpty()) {
            showError(getString(R.string.error_field_required));
            binding.emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            binding.emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showError(getString(R.string.error_field_required));
            binding.passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showError(getString(R.string.error_password_too_short));
            binding.passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            showError(getString(R.string.error_invalid_email));
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showError(getString(R.string.error_invalid_password));
        } else {
            showError(getString(R.string.login_error));
        }
    }

    private void signInWithGoogle() {
        setLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                setLoading(false);
                showError(getString(R.string.google_signin_failed));
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        showError(getString(R.string.auth_failed));
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!isLoading);
        binding.googleSignInButton.setEnabled(!isLoading);
        binding.registerButton.setEnabled(!isLoading);
        binding.emailInput.setEnabled(!isLoading);
        binding.passwordInput.setEnabled(!isLoading);
    }

    private void navigateToMain() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Línea eliminada: ((MiGymApplication) getApplication()).getUserPreferences().saveUserData(user.getUid(), user.getEmail(), user.getDisplayName());
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 