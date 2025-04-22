package com.example.migym.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.migym.models.User;
import com.example.migym.repositories.UserRepository;
import com.example.migym.repositories.UserRepository.OnProfileUpdateListener;
import com.example.migym.utils.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private final UserRepository repository;
    private final UserPreferences userPreferences;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Double> uploadProgress = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<>(false);

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        userPreferences = new UserPreferences(application);
        repository.getCurrentUser().observeForever(user -> currentUser.postValue(user));
        loadCurrentUser();
    }

    public void uploadProfileImage(Uri photoUri, OnProfileUpdateListener listener) {
        if (photoUri == null) {
            errorMessage.setValue("No image selected");
            return;
        }

        isLoading.setValue(true);
        final OnProfileUpdateListener finalListener = listener;
        repository.uploadProfileImage(photoUri, new OnProfileUpdateListener() {
            @Override
            public void onSuccess(String imageUrl) {
                isLoading.postValue(false);
                uploadProgress.postValue(0.0);
                if (finalListener != null) {
                    finalListener.onSuccess(imageUrl);
                }
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                uploadProgress.postValue(0.0);
                errorMessage.postValue(error);
                if (finalListener != null) {
                    finalListener.onError(error);
                }
            }

            @Override
            public void onProgress(double progress) {
                uploadProgress.postValue(progress);
                if (finalListener != null) {
                    finalListener.onProgress(progress);
                }
            }
        });
    }

    private User createUpdatedUser(String name, String email) {
        User updatedUser = new User();
        updatedUser.setName(name);
        updatedUser.setEmail(email);
        
        User currentUserValue = currentUser.getValue();
        if (currentUserValue != null) {
            updatedUser.setWeight(currentUserValue.getWeight());
            updatedUser.setHeight(currentUserValue.getHeight());
            updatedUser.setAge(currentUserValue.getAge());
            updatedUser.setHeartProblems(currentUserValue.hasHeartProblems());
            updatedUser.setHeartProblemsDetails(currentUserValue.getHeartProblemsDetails());
            updatedUser.setPhotoUrl(currentUserValue.getPhotoUrl());
        }
        
        return updatedUser;
    }

    public void updateProfile(String name, String email) {
        final User updatedUser = createUpdatedUser(name, email);
        
        repository.updateUserProfile(updatedUser, new UserRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess(String imageUrl) {
                currentUser.postValue(updatedUser);
                errorMessage.postValue(null);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }

            @Override
            public void onProgress(double progress) {
                uploadProgress.postValue(progress);
            }
        });
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Double> getUploadProgress() {
        return uploadProgress;
    }

    public LiveData<Boolean> getIsLoggedOut() {
        return isLoggedOut;
    }

    public void logout() {
        isLoading.setValue(true);
        
        // Cerrar sesión en Firebase primero
        FirebaseAuth.getInstance().signOut();
        
        // Limpiar todos los datos locales
        userPreferences.clearUserData();
        userPreferences.setLoggedIn(false);
        
        // Actualizar el estado de la UI
        isLoggedOut.postValue(true);
        isLoading.postValue(false);
        errorMessage.postValue(null);
        currentUser.postValue(null);
        
        Log.d(TAG, "Sesión cerrada correctamente");
    }

    public boolean isLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean isFirebaseLoggedIn = auth.getCurrentUser() != null;
        
        // Sincronizar el estado con UserPreferences
        if (isFirebaseLoggedIn != userPreferences.isLoggedIn()) {
            userPreferences.setLoggedIn(isFirebaseLoggedIn);
        }
        
        Log.d(TAG, "Estado de autenticación - Firebase: " + isFirebaseLoggedIn + 
              ", Preferences: " + userPreferences.isLoggedIn());
        
        return isFirebaseLoggedIn;
    }

    private void loadCurrentUser() {
        if (!isLoggedIn()) {
            currentUser.postValue(null);
            return;
        }
        
        User user = new User();
        user.setName(userPreferences.getName());
        user.setEmail(userPreferences.getEmail());
        user.setWeight(userPreferences.getWeight());
        user.setHeight(userPreferences.getHeight());
        user.setAge(userPreferences.getAge());
        user.setHeartProblems(userPreferences.hasHeartProblems());
        user.setHeartProblemsDetails(userPreferences.getHeartProblemsDetails());
        user.setPhotoUrl(userPreferences.getProfileImageUrl());
        currentUser.postValue(user);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
} 