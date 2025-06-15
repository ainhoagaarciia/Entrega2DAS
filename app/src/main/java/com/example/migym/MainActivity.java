package com.example.migym;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.migym.databinding.ActivityMainBinding;
import com.example.migym.ui.auth.LoginActivity;
import com.example.migym.viewmodels.UserViewModel;
import com.google.android.material.navigation.NavigationView;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import com.example.migym.repositories.UserRepository;
import com.example.migym.models.User;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawerLayout;
    private UserViewModel userViewModel;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if user is not authenticated
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user found, redirecting to LoginActivity");
            startLoginActivity();
            return;
        }

        Log.d(TAG, "User authenticated: " + currentUser.getEmail());

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Load user profile from Firebase
        userViewModel.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                Log.d(TAG, "User profile loaded from Firebase");
                // Update local database with Firebase data
                userViewModel.updateUserProfile(user);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading user profile: " + error);
            }
        });

        // Setup UI components
        setupUI();

        // Setup authentication state listener
        setupAuthStateListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add the auth state listener
        if (auth != null) {
            auth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove the auth state listener
        if (auth != null && authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Log.d(TAG, "User signed out, redirecting to LoginActivity");
                startLoginActivity();
            } else {
                Log.d(TAG, "Auth state changed, user: " + user.getEmail());
                // Si estamos en MainActivity y el usuario está autenticado, no hacer nada
            }
        };
    }

    private void startLoginActivity() {
        Log.d(TAG, "Starting LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        
        // Setup drawer layout
        drawerLayout = binding.drawerLayout;
        
        // Setup Navigation
        setupNavigation();
        
        // Apply settings
        applySettings();
        
        // Handle back button
        setupBackNavigation();

        // Observe logout state
        observeLogoutState();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(binding.navView)) {
                    drawerLayout.closeDrawer(binding.navView);
                } else if (navController.getCurrentDestination() != null && 
                         navController.getCurrentDestination().getId() != R.id.nav_home) {
                    navController.navigate(R.id.nav_home);
                } else {
                    showLogoutConfirmation();
                }
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                userViewModel.logout();
                redirectToLogin();
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void observeLogoutState() {
        userViewModel.getIsLoggedOut().observe(this, isLoggedOut -> {
            if (isLoggedOut) {
                redirectToLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // No verificar autenticación en onResume para evitar bucles
        if (navController != null && navController.getCurrentDestination() == null) {
            navController.navigate(R.id.nav_home);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear back stack to prevent returning to MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        Log.d(TAG, "Iniciando configuración de navegación");
        
        try {
            // Get NavHostFragment
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);
            
            if (navHostFragment == null) {
                Log.e(TAG, "NavHostFragment no encontrado");
                return;
            }
            
            // Get NavController from NavHostFragment
            navController = navHostFragment.getNavController();
            
            // Configure the top-level destinations
            appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_workout, R.id.nav_profile, R.id.nav_settings)
                .setOpenableLayout(drawerLayout)
                .build();

            // Setup ActionBar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            
            // Setup NavigationView with NavController
            NavigationUI.setupWithNavController(binding.navView, navController);
            
            // Set default destination to home
            if (navController.getCurrentDestination() == null) {
                navController.navigate(R.id.nav_home);
            }
            
            Log.d(TAG, "Navegación configurada correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error en setupNavigation", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> userViewModel.logout())
                .setNegativeButton(R.string.no, null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void applySettings() {
        try {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            
            // Apply night mode
            int nightMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            AppCompatDelegate.setDefaultNightMode(nightMode);
            
            // Apply language
            String language = prefs.getString("language", null);
            if (language != null) {
                Locale locale = new Locale(language);
                Locale.setDefault(locale);
                Configuration config = new Configuration(getResources().getConfiguration());
                config.setLocale(locale);
                
                // Create a new context with the updated configuration
                Context context = createConfigurationContext(config);
                
                // Update the application context
                getApplicationContext().createConfigurationContext(config);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error aplicando configuración: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        navController = null;
    }
} 
