package com.example.migym.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.example.migym.R;
import com.example.migym.databinding.FragmentProfileBinding;
import com.example.migym.repositories.UserRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Environment;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.DataSource;
import android.text.Editable;
import android.text.TextWatcher;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentProfileBinding binding;
    private Uri selectedImageUri;
    private Uri photoURI;
    private UserRepository userRepository;
    private boolean isUploading = false;

    private final ActivityResultLauncher<String> selectPicture = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                handleNewPhoto(uri);
            }
        }
    );

    private final ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                selectPicture.launch("image/*");
            } else {
                showSnackbar(getString(R.string.gallery_permission_denied));
            }
        }
    );

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    );

    private final ActivityResultLauncher<Intent> takePicture = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                uploadProfileImage();
            }
        }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            userRepository = new UserRepository(requireContext());
            setupClickListeners();
            setupListeners();
            // Cargar SIEMPRE los datos desde Firestore
            userRepository.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(com.example.migym.models.User user) {
                    // Actualizar la UI SOLO con datos de Firestore
                    binding.nameInput.setText(user.getName() != null ? user.getName() : "");
                    binding.ageInput.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
                    binding.weightInput.setText(user.getWeight() > 0 ? String.valueOf(user.getWeight()) : "");
                    binding.heightInput.setText(user.getHeight() > 0 ? String.valueOf(user.getHeight()) : "");
                    binding.genderSpinner.setSelection(user.getGender());
                    binding.heartProblemsSwitch.setChecked(user.hasHeartProblems());
                    binding.heartProblemsDetailsInput.setText(user.getHeartProblemsDetails() != null ? user.getHeartProblemsDetails() : "");
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        Log.d("ProfileFragment", "Cargando imagen de perfil: " + user.getPhotoUrl());
                        Toast.makeText(requireContext(), "URL de imagen: " + user.getPhotoUrl(), Toast.LENGTH_SHORT).show();
                        Glide.with(ProfileFragment.this)
                            .load(user.getPhotoUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.profileImage);
                    } else {
                        Log.d("ProfileFragment", "No hay foto de perfil, se muestra la predeterminada");
                        binding.profileImage.setImageResource(R.drawable.default_profile);
                    }
                }
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error cargando perfil de Firestore: " + error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            showSnackbar(getString(R.string.error_loading_profile));
        }
    }

    private void setupListeners() {
        if (binding == null) return;
        binding.heartProblemsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.heartProblemsDetailsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            saveProfileData();
        });
        binding.nameInput.addTextChangedListener(new SimpleTextWatcher(this::saveProfileData));
        binding.ageInput.addTextChangedListener(new SimpleTextWatcher(this::saveProfileData));
        binding.weightInput.addTextChangedListener(new SimpleTextWatcher(this::saveProfileData));
        binding.heightInput.addTextChangedListener(new SimpleTextWatcher(this::saveProfileData));
        binding.heartProblemsDetailsInput.addTextChangedListener(new SimpleTextWatcher(this::saveProfileData));
    }

    private void setupClickListeners() {
        binding.profileImage.setOnClickListener(v -> showImagePickerDialog());
    }

    private void checkPermissionsAndPickImage() {
        if (getContext() == null) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission.launch(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    selectPicture.launch("image/*");
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    selectPicture.launch("image/*");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking permissions", e);
            showSnackbar(getString(R.string.error_loading_image));
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                handleNewPhoto(selectedImageUri);
            }
        }
    }

    private void checkStoragePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            selectPicture.launch("image/*");
        } else {
            requestStoragePermission.launch(permission);
        }
    }

    private void checkCameraPermission() {
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(requireContext(), R.string.error_creating_image, Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.example.migym.fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePicture.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );
    }

    private void showImagePickerDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(view);

        View takePhoto = view.findViewById(R.id.take_photo);
        View chooseGallery = view.findViewById(R.id.choose_gallery);

        takePhoto.setOnClickListener(v -> {
            dialog.dismiss();
            if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            } else {
                startCamera();
            }
        });

        chooseGallery.setOnClickListener(v -> {
            dialog.dismiss();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission.launch(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    selectPicture.launch("image/*");
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    selectPicture.launch("image/*");
                }
            }
        });

        dialog.show();
    }

    private void handleNewPhoto(Uri uri) {
        if (uri == null) {
            Toast.makeText(requireContext(), "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressBar.setProgress(0);

        userRepository.uploadProfileImage(uri, new UserRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess(String imageUrl) {
                userRepository.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
                    @Override
                    public void onUserLoaded(com.example.migym.models.User user) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            Log.d("ProfileFragment", "Cargando imagen de perfil: " + user.getPhotoUrl());
                            Toast.makeText(requireContext(), "URL de imagen: " + user.getPhotoUrl(), Toast.LENGTH_SHORT).show();
                            Glide.with(ProfileFragment.this)
                                .load(user.getPhotoUrl())
                                .circleCrop()
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(binding.profileImage);
                        } else {
                            Log.d("ProfileFragment", "No hay foto de perfil, se muestra la predeterminada");
                            binding.profileImage.setImageResource(R.drawable.default_profile);
                        }
                        Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(double progress) {
                binding.progressBar.setProgress((int) (progress * 100));
            }
        });
    }

    private void loadProfileImageFromUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            binding.profileImage.setImageResource(R.drawable.default_profile);
            return;
        }

        Glide.with(this)
            .load(imagePath)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.profileImage);
    }

    private void loadProfileData() {
        if (binding == null) return;

        try {
            binding.nameInput.setText(binding.nameInput.getText() != null ? binding.nameInput.getText().toString().trim() : "");
            binding.ageInput.setText(binding.ageInput.getText() != null ? binding.ageInput.getText().toString().trim() : "");
            binding.weightInput.setText(binding.weightInput.getText() != null ? binding.weightInput.getText().toString().trim() : "");
            binding.heightInput.setText(binding.heightInput.getText() != null ? binding.heightInput.getText().toString().trim() : "");
            binding.genderSpinner.setSelection(binding.genderSpinner.getSelectedItemPosition());
            binding.heartProblemsSwitch.setChecked(binding.heartProblemsSwitch.isChecked());
            binding.heartProblemsDetailsLayout.setVisibility(binding.heartProblemsSwitch.isChecked() ? View.VISIBLE : View.GONE);
            binding.heartProblemsDetailsInput.setText(binding.heartProblemsDetailsInput.getText() != null ? binding.heartProblemsDetailsInput.getText().toString().trim() : "");
            
            String imagePath = binding.heartProblemsSwitch.isChecked() ? binding.heartProblemsDetailsInput.getText().toString().trim() : null;
            Log.d(TAG, "Loading profile image from path: " + imagePath);
            if (imagePath != null && !imagePath.isEmpty()) {
                loadProfileImageFromUrl(imagePath);
            } else {
                binding.profileImage.setImageResource(R.drawable.default_profile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile data", e);
            showSnackbar(getString(R.string.error_loading_profile));
        }
    }

    private void saveProfileData() {
        // Obtener los valores de los campos
        String name = getTextFromInput(binding.nameInput);
        String ageStr = getTextFromInput(binding.ageInput);
        String weightStr = getTextFromInput(binding.weightInput);
        String heightStr = getTextFromInput(binding.heightInput);
        int genderIndex = binding.genderSpinner.getSelectedItemPosition();
        boolean hasHeartProblems = binding.heartProblemsSwitch.isChecked();
        String heartProblemsDetails = getTextFromInput(binding.heartProblemsDetailsInput);

        // Obtener el usuario actual para mantener el photoUrl
        userRepository.getCurrentUser().observe(getViewLifecycleOwner(), currentUser -> {
            com.example.migym.models.User user = new com.example.migym.models.User();
            user.setName(name);
            try { user.setAge(Integer.parseInt(ageStr)); } catch (Exception ignored) {}
            try { user.setWeight(Double.parseDouble(weightStr)); } catch (Exception ignored) {}
            try { user.setHeight(Double.parseDouble(heightStr)); } catch (Exception ignored) {}
            user.setGender(genderIndex);
            user.setHeartProblems(hasHeartProblems);
            user.setHeartProblemsDetails(heartProblemsDetails);
            if (currentUser != null && currentUser.getPhotoUrl() != null) {
                user.setPhotoUrl(currentUser.getPhotoUrl());
            }
            userRepository.saveUserProfileToFirebase(user, new UserRepository.OnProfileUpdateListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "Perfil guardado en Firestore");
                }
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error guardando perfil en Firestore: " + error);
                }
                @Override
                public void onProgress(double progress) {}
            });
        });
    }

    private String getTextFromInput(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void showSnackbar(String message) {
        if (getView() != null) {
            try {
                Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing snackbar", e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPicture.launch("image/*");
            } else {
                Snackbar.make(requireView(), R.string.storage_permission_required, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void uploadProfileImage() {
        if (photoURI == null) return;

        isUploading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressBar.setProgress(0);
        binding.profileImage.setEnabled(false);

        // Mostrar la imagen capturada inmediatamente
        Glide.with(this)
            .load(photoURI)
            .into(binding.profileImage);

        userRepository.uploadProfileImage(photoURI, new UserRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess(String imageUrl) {
                if (!isAdded()) return;
                isUploading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.profileImage.setEnabled(true);

                // Recargar el usuario desde Firestore para obtener el photoUrl actualizado
                userRepository.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
                    @Override
                    public void onUserLoaded(com.example.migym.models.User user) {
                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            Log.d(TAG, "Cargando imagen de perfil: " + user.getPhotoUrl());
                            Glide.with(ProfileFragment.this)
                                .load(user.getPhotoUrl())
                                .circleCrop()
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(binding.profileImage);
                        } else {
                            Log.d(TAG, "No hay foto de perfil, se muestra la predeterminada");
                            binding.profileImage.setImageResource(R.drawable.default_profile);
                        }
                        Toast.makeText(requireContext(), R.string.profile_image_updated, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                
                isUploading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.profileImage.setEnabled(true);
                
                // Mostrar mensaje de error
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                    .setAction("Reintentar", v -> uploadProfileImage())
                    .show();
            }

            @Override
            public void onProgress(double progress) {
                if (!isAdded()) return;
                binding.progressBar.setProgress((int)(progress * 100));
            }
        });
    }

    // SimpleTextWatcher para ahorrar código
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable callback;
        SimpleTextWatcher(Runnable callback) { this.callback = callback; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(android.text.Editable s) { callback.run(); }
    }

    @Override
    public void onResume() {
        super.onResume();
        userRepository.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(com.example.migym.models.User user) {
                Log.d("ProfileFragment", "Recargando usuario en onResume: " + user.getPhotoUrl());
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    Glide.with(ProfileFragment.this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.profileImage);
                } else {
                    Log.d("ProfileFragment", "No hay foto de perfil en onResume, se muestra la predeterminada");
                    binding.profileImage.setImageResource(R.drawable.default_profile);
                }
            }
            @Override
            public void onError(String error) {
                Log.e("ProfileFragment", "Error recargando usuario: " + error);
            }
        });
    }
} 