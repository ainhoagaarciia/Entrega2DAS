package com.example.migym.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.example.migym.R;
import com.google.firebase.auth.FirebaseAuth;
import com.example.migym.ui.login.LoginActivity;
import org.jetbrains.annotations.Nullable;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        
        Preference logoutPreference = findPreference("logout");
        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                showLogoutConfirmationDialog();
                return true;
            });
        }
    }

    private void showLogoutConfirmationDialog() {
        Context context = requireContext();
        new AlertDialog.Builder(context)
            .setTitle(R.string.logout_confirmation)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes, (dialog, which) -> handleLogout())
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void handleLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Show success message
        Context context = requireContext();
        Toast.makeText(context, R.string.logout_success, Toast.LENGTH_SHORT).show();
        
        // Create login intent with application context
        Intent loginIntent = new Intent(requireContext().getApplicationContext(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        requireContext().startActivity(loginIntent);
    }
} 