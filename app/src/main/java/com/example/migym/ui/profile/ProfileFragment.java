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

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentProfileBinding binding;
    private SharedPreferences prefs;
    private Uri selectedImageUri;
    private Uri photoURI;
    private UserRepository userRepository;
    private boolean isUploading = false;
    private static final String PREF_PROFILE_NAME = "profile_name";
    private static final String PREF_PROFILE_AGE = "profile_age";
    private static final String PREF_PROFILE_WEIGHT = "profile_weight";
    private static final String PREF_PROFILE_HEIGHT = "profile_height";
    private static final String PREF_PROFILE_HEART_PROBLEMS = "profile_heart_problems";
    private static final String PREF_PROFILE_HEART_DETAILS = "profile_heart_problems_details";
    private static final String PREF_PROFILE_IMAGE_PATH = "profile_image_path";
    private static final String PREF_PROFILE_GENDER = "profile_gender";

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
            prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            
            setupClickListeners();
            setupListeners();
            // Sincronizar con Firestore al abrir el perfil
            userRepository.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(com.example.migym.models.User user) {
                    // Actualizar la UI con los datos de Firestore
                    binding.nameInput.setText(user.getName() != null ? user.getName() : "");
                    binding.ageInput.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
                    binding.weightInput.setText(user.getWeight() > 0 ? String.valueOf(user.getWeight()) : "");
                    binding.heightInput.setText(user.getHeight() > 0 ? String.valueOf(user.getHeight()) : "");
                    binding.genderSpinner.setSelection(user.getGender());
                    binding.heartProblemsSwitch.setChecked(user.hasHeartProblems());
                    binding.heartProblemsDetailsInput.setText(user.getHeartProblemsDetails() != null ? user.getHeartProblemsDetails() : "");
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        Glide.with(ProfileFragment.this)
                            .load(user.getPhotoUrl())
                            .circleCrop()
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(binding.profileImage);
                    }
                }
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error cargando perfil de Firestore: " + error);
                }
            });
            loadProfileData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            showSnackbar(getString(R.string.error_loading_profile));
        }
    }

    private void setupListeners() {
        if (binding == null) return;

        // Eliminar el listener del botón eliminado
        // binding.changePhotoButton.setOnClickListener(v -> {
        //     if (!isUploading) {
        //         showImagePickerDialog();
        //     } else {
        //         Toast.makeText(requireContext(), R.string.upload_in_progress, Toast.LENGTH_SHORT).show();
        //     }
        // });
        
        binding.heartProblemsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.heartProblemsDetailsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            saveProfileData();
        });

        // Configurar listeners para el guardado automático
        binding.nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveProfileData();
            }
        });

        binding.ageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveProfileData();
            }
        });

        binding.weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveProfileData();
            }
        });

        binding.heightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveProfileData();
            }
        });

        binding.heartProblemsDetailsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveProfileData();
            }
        });
    }

    private void setupClickListeners() {
        // Al tocar la imagen, mostrar el diálogo para elegir galería o cámara
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
            Toast.makeText(requireContext(), "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedImageUri = uri;

        // Mostrar la imagen seleccionada inmediatamente
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.profileImage);

        // Subir la imagen al servidor
        isUploading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        // binding.changePhotoButton.setEnabled(false); // Eliminar referencia

        userRepository.uploadProfileImage(uri, new UserRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess(String imageUrl) {
                requireActivity().runOnUiThread(() -> {
                    isUploading = false;
                    binding.progressBar.setVisibility(View.GONE);
                    // binding.changePhotoButton.setEnabled(true); // Eliminar referencia

                    // Guardar la URL de la imagen en las preferencias
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREF_PROFILE_IMAGE_PATH, imageUrl);
                    editor.apply();

                    Log.d(TAG, "Image URL saved to preferences: " + imageUrl);

                    // Cargar la imagen desde la URL del servidor
                    loadProfileImageFromUrl(imageUrl);

                    Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    isUploading = false;
                    binding.progressBar.setVisibility(View.GONE);
                    // binding.changePhotoButton.setEnabled(true); // Eliminar referencia
                    Toast.makeText(requireContext(), "Error al subir la imagen: " + error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(double progress) {
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setProgress((int) (progress * 100));
                });
            }
        });
    }

    private void loadProfileData() {
        if (binding == null || prefs == null) return;

        try {
            binding.nameInput.setText(prefs.getString(PREF_PROFILE_NAME, ""));
            binding.ageInput.setText(prefs.getString(PREF_PROFILE_AGE, ""));
            binding.weightInput.setText(prefs.getString(PREF_PROFILE_WEIGHT, ""));
            binding.heightInput.setText(prefs.getString(PREF_PROFILE_HEIGHT, ""));
            // Cargar género
            int genderIndex = prefs.getInt(PREF_PROFILE_GENDER, 0);
            binding.genderSpinner.setSelection(genderIndex);
            
            boolean hasHeartProblems = prefs.getBoolean(PREF_PROFILE_HEART_PROBLEMS, false);
            binding.heartProblemsSwitch.setChecked(hasHeartProblems);
            binding.heartProblemsDetailsLayout.setVisibility(hasHeartProblems ? View.VISIBLE : View.GONE);
            binding.heartProblemsDetailsInput.setText(prefs.getString(PREF_PROFILE_HEART_DETAILS, ""));
            
            String imagePath = prefs.getString(PREF_PROFILE_IMAGE_PATH, null);
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

        // Guardar los datos en SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_PROFILE_NAME, name);
        editor.putString(PREF_PROFILE_AGE, ageStr);
        editor.putString(PREF_PROFILE_WEIGHT, weightStr);
        editor.putString(PREF_PROFILE_HEIGHT, heightStr);
        editor.putInt(PREF_PROFILE_GENDER, genderIndex);
        editor.putBoolean(PREF_PROFILE_HEART_PROBLEMS, hasHeartProblems);
        editor.putString(PREF_PROFILE_HEART_DETAILS, heartProblemsDetails);
        editor.apply();

        // Guardar los datos en Firestore
        com.example.migym.models.User user = new com.example.migym.models.User();
        user.setName(name);
        try { user.setAge(Integer.parseInt(ageStr)); } catch (Exception ignored) {}
        try { user.setWeight(Double.parseDouble(weightStr)); } catch (Exception ignored) {}
        try { user.setHeight(Double.parseDouble(heightStr)); } catch (Exception ignored) {}
        user.setGender(genderIndex);
        user.setHeartProblems(hasHeartProblems);
        user.setHeartProblemsDetails(heartProblemsDetails);
        // Si tienes email o photoUrl, añádelos aquí
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

        // Si hay una nueva imagen seleccionada, subirla
        if (selectedImageUri != null) {
            isUploading = true;
            binding.progressBar.setVisibility(View.VISIBLE);
            // binding.changePhotoButton.setEnabled(false); // Eliminar referencia

            userRepository.uploadProfileImage(selectedImageUri, new UserRepository.OnProfileUpdateListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    requireActivity().runOnUiThread(() -> {
                        // Guardar la URL de la imagen en las preferencias
                        prefs.edit().putString(PREF_PROFILE_IMAGE_PATH, imageUrl).apply();
                        
                        // Cargar la imagen desde la URL
                        loadProfileImageFromUrl(imageUrl);
                        
                        // Restaurar la UI
                        binding.progressBar.setVisibility(View.GONE);
                        // binding.changePhotoButton.setEnabled(true); // Eliminar referencia
                        isUploading = false;
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        // binding.changePhotoButton.setEnabled(true); // Eliminar referencia
                        isUploading = false;
                        Toast.makeText(requireContext(), "Error al actualizar la imagen: " + error, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onProgress(double progress) {
                    requireActivity().runOnUiThread(() -> {
                        binding.progressBar.setProgress((int) (progress * 100));
                    });
                }
            });
        }
    }

    private String getTextFromInput(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void loadProfileImageFromUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.w(TAG, "Image path is null or empty");
            binding.profileImage.setImageResource(R.drawable.default_profile);
            return;
        }

        Log.d(TAG, "Loading profile image from path: " + imagePath);

        // Crear un File si es una ruta local
        File imageFile = new File(imagePath);
        Object imageSource = imagePath.startsWith("http") ? imagePath : imageFile;

        Glide.with(this)
            .load(imageSource)
            .circleCrop()
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                          Target<Drawable> target, boolean isFirstResource) {
                    Log.e(TAG, "Error loading image from path: " + imagePath, e);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, 
                                             Target<Drawable> target, DataSource dataSource, 
                                             boolean isFirstResource) {
                    Log.d(TAG, "Image loaded successfully from path: " + imagePath);
                    return false;
                }
            })
            .into(binding.profileImage);
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
        // binding.changePhotoButton.setEnabled(false); // Eliminar referencia

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
                // binding.changePhotoButton.setEnabled(true); // Eliminar referencia

                // Guardar la URL de la imagen en las preferencias
                prefs.edit().putString(PREF_PROFILE_IMAGE_PATH, imageUrl).apply();

                // Cargar la imagen desde la URL del servidor
                Glide.with(ProfileFragment.this)
                    .load(imageUrl)
                    .error(R.drawable.default_profile)
                    .into(binding.profileImage);

                Toast.makeText(requireContext(), R.string.profile_image_updated, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                
                isUploading = false;
                binding.progressBar.setVisibility(View.GONE);
                // binding.changePhotoButton.setEnabled(true); // Eliminar referencia
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(double progress) {
                if (!isAdded()) return;
                binding.progressBar.setProgress((int)(progress * 100));
            }
        });
    }
} 