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
import com.google.firebase.auth.FirebaseAuth;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private final UserRepository userRepository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Double> uploadProgress = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoggedOut;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
        isLoggedOut = new MutableLiveData<>(false);
        userRepository.getCurrentUser().observeForever(user -> currentUser.postValue(user));
        loadCurrentUser();
    }

    public void uploadProfileImage(Uri photoUri, OnProfileUpdateListener listener) {
        if (photoUri == null) {
            errorMessage.setValue("No image selected");
            return;
        }

        isLoading.setValue(true);
        final OnProfileUpdateListener finalListener = listener;
        userRepository.uploadProfileImage(photoUri, new OnProfileUpdateListener() {
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
        
        userRepository.updateUserProfile(updatedUser, new UserRepository.OnProfileUpdateListener() {
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
        userRepository.logout(new UserRepository.OnLogoutListener() {
            @Override
            public void onSuccess() {
                isLoggedOut.postValue(true);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }
        });
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void loadCurrentUser() {
        if (!isLoggedIn()) {
            currentUser.postValue(null);
            return;
        }
        
        userRepository.loadUserProfileFromFirebase(new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser.postValue(user);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }
        });
    }

    public void loadUserProfileFromFirebase(UserRepository.OnUserLoadedListener listener) {
        userRepository.loadUserProfileFromFirebase(listener);
    }

    public void updateUserProfile(User user) {
        userRepository.updateUserProfile(user, new UserRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess(String imageUrl) {
                currentUser.postValue(user);
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

    @Override
    protected void onCleared() {
        super.onCleared();
    }
} 