package com.example.migym.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.migym.R;
import com.example.migym.ui.auth.LoginActivity;
import com.example.migym.viewmodels.UserViewModel;

public class SettingsFragment extends PreferenceFragmentCompat {
    private UserViewModel userViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Setup logout preference
        Preference logoutPreference = findPreference("logout");
        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                userViewModel.logout();
                // Navigate to login screen
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            });
        }
    }
} 