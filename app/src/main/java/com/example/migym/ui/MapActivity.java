package com.example.migym.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.migym.R;
import com.example.migym.databinding.ActivityMapBinding;
import com.example.migym.utils.FirebaseStorageManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    
    private ActivityMapBinding binding;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LatLng selectedLocation;
    private boolean locationPermissionGranted = false;
    private boolean isRequestingLocationUpdates = false;
    private Geocoder geocoder;
    private MaterialButton confirmButton;
    private FirebaseStorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageManager = new FirebaseStorageManager();

        // Add back press handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.select_location);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        setupLocationCallback();
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        confirmButton = binding.confirmButton;
        confirmButton.setEnabled(false);
        confirmButton.setOnClickListener(v -> confirmLocation());
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && map != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (selectedLocation == null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                        stopLocationUpdates();
                    }
                }
            }
        };
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        
        map.setOnMapClickListener(this);

        if (checkLocationPermission()) {
            setupMap();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, R.string.location_permission_rationale, Toast.LENGTH_LONG).show();
        }
        
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                updateLocationUI();
                getDeviceLocation();
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error updating location UI: " + e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                startLocationUpdates();
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                            );
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                        } else {
                            // Si no hay última ubicación conocida, solicitar actualizaciones
                            startLocationUpdates();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location: " + e.getMessage());
                        Toast.makeText(this, R.string.location_error, Toast.LENGTH_SHORT).show();
                    });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error getting device location: " + e.getMessage());
            Toast.makeText(this, R.string.location_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSelectedLocation(LatLng latLng) {
        selectedLocation = latLng;
        map.clear();
        map.addMarker(new MarkerOptions()
            .position(latLng)
            .title(getString(R.string.selected_location)));
        confirmButton.setEnabled(true);
        
        // Mostrar la dirección seleccionada
        updateAddressText(latLng);
    }

    private void updateAddressText(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressStr = new StringBuilder();
                
                // Add address line if available
                if (address.getMaxAddressLineIndex() >= 0) {
                    addressStr.append(address.getAddressLine(0));
                } else {
                    // Fallback to composing address from components
                if (address.getThoroughfare() != null) {
                        addressStr.append(address.getThoroughfare());
                    }
                    if (address.getSubThoroughfare() != null) {
                        addressStr.append(" ").append(address.getSubThoroughfare());
                    }
                    if (address.getLocality() != null) {
                        addressStr.append(", ").append(address.getLocality());
                    }
                }
                
                binding.selectedLocationText.setText(addressStr.toString());
                binding.selectedLocationText.setVisibility(View.VISIBLE);
            } else {
                binding.selectedLocationText.setText(R.string.geocoding_error);
                binding.selectedLocationText.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
            binding.selectedLocationText.setVisibility(View.GONE);
        }
    }

    private void startLocationUpdates() {
        if (!locationPermissionGranted) return;

        try {
            LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                isRequestingLocationUpdates = true;
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error requesting location updates: " + e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        if (isRequestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isRequestingLocationUpdates = false;
        }
    }

    private void setupMap() {
        if (map == null) return;

        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), 
                                    location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    })
                    .addOnFailureListener(e -> 
                            Toast.makeText(this, R.string.location_error, Toast.LENGTH_SHORT).show());
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, R.string.select_location_first, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedLocation.latitude);
        resultIntent.putExtra("longitude", selectedLocation.longitude);

        // Obtener ciudad, calle y dirección completa usando Geocoder
        String city = "";
        String street = "";
        String addressLine = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(selectedLocation.latitude, selectedLocation.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) city = address.getLocality();
                if (address.getThoroughfare() != null) street = address.getThoroughfare();
                if (address.getMaxAddressLineIndex() >= 0) addressLine = address.getAddressLine(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting address for location", e);
        }
        resultIntent.putExtra("city", city);
        resultIntent.putExtra("street", street);
        resultIntent.putExtra("address", addressLine);
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationPermissionGranted && selectedLocation == null) {
            startLocationUpdates();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        selectedLocation = latLng;
        map.clear();
        map.addMarker(new MarkerOptions()
            .position(latLng)
            .title(getString(R.string.selected_location)));
        confirmButton.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
} 