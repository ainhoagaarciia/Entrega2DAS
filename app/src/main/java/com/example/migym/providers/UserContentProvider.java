package com.example.migym.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.migym.models.User;
import com.example.migym.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class UserContentProvider extends ContentProvider {
    private static final String TAG = "UserContentProvider";
    private static final String AUTHORITY = "com.example.migym.provider";
    private static final String PATH_USERS = "users";
    
    private static final int USERS = 1;
    private static final int USER_ID = 2;
    
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, PATH_USERS, USERS);
        uriMatcher.addURI(AUTHORITY, PATH_USERS + "/#", USER_ID);
    }
    
    private UserRepository userRepository;
    
    @Override
    public boolean onCreate() {
        userRepository = new UserRepository(getContext());
        return true;
    }
    
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                       @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[] {
            "id", "name", "email", "photoUrl", "weight", "height", "age"
        });
        
        switch (uriMatcher.match(uri)) {
            case USERS:
                User user = userRepository.getCurrentUser().getValue();
                if (user != null) {
                    cursor.addRow(new Object[] {
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhotoUrl(),
                        user.getWeight(),
                        user.getHeight(),
                        user.getAge()
                    });
                }
                break;
                
            case USER_ID:
                String userId = uri.getLastPathSegment();
                if (userId != null && userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    User currentUser = userRepository.getCurrentUser().getValue();
                    if (currentUser != null) {
                        cursor.addRow(new Object[] {
                            currentUser.getId(),
                            currentUser.getName(),
                            currentUser.getEmail(),
                            currentUser.getPhotoUrl(),
                            currentUser.getWeight(),
                            currentUser.getHeight(),
                            currentUser.getAge()
                        });
                    }
                }
                break;
        }
        
        return cursor;
    }
    
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
    
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) {
            return null;
        }
        
        switch (uriMatcher.match(uri)) {
            case USERS:
                String name = values.getAsString("name");
                String email = values.getAsString("email");
                String photoUrl = values.getAsString("photoUrl");
                Double weight = values.getAsDouble("weight");
                Double height = values.getAsDouble("height");
                Integer age = values.getAsInteger("age");
                
                if (name != null && email != null) {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setPhotoUrl(photoUrl);
                    user.setWeight(weight != null ? weight : 0.0);
                    user.setHeight(height != null ? height : 0.0);
                    user.setAge(age != null ? age : 0);
                    
                    // Actualizar en Firestore
                    userRepository.updateUserProfile(user, new UserRepository.OnProfileUpdateListener() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            Log.d(TAG, "User profile updated successfully");
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error updating user profile: " + error);
                        }
                        
                        @Override
                        public void onProgress(double progress) {
                            // No se usa para actualizaciones de perfil
                        }
                    });
                    
                    return Uri.parse("content://" + AUTHORITY + "/" + PATH_USERS + "/" + user.getId());
                }
                break;
        }
        
        return null;
    }
    
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case USER_ID:
                String userId = uri.getLastPathSegment();
                if (userId != null && userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    return 1;
                }
                break;
        }
        
        return 0;
    }
    
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                     @Nullable String[] selectionArgs) {
        if (values == null) {
            return 0;
        }
        
        switch (uriMatcher.match(uri)) {
            case USER_ID:
                String userId = uri.getLastPathSegment();
                if (userId != null && userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    String name = values.getAsString("name");
                    Double weight = values.getAsDouble("weight");
                    Double height = values.getAsDouble("height");
                    Integer age = values.getAsInteger("age");
                    String photoUrl = values.getAsString("photoUrl");
                    
                    User user = userRepository.getCurrentUser().getValue();
                    if (user != null) {
                        if (name != null) user.setName(name);
                        if (weight != null) user.setWeight(weight);
                        if (height != null) user.setHeight(height);
                        if (age != null) user.setAge(age);
                        if (photoUrl != null) user.setPhotoUrl(photoUrl);
                        
                        // Actualizar en Firestore
                        userRepository.updateUserProfile(user, new UserRepository.OnProfileUpdateListener() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                Log.d(TAG, "User profile updated successfully");
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error updating user profile: " + error);
                            }
                            
                            @Override
                            public void onProgress(double progress) {
                                // No se usa para actualizaciones de perfil
                            }
                        });
                        
                        return 1;
                    }
                }
                break;
        }
        
        return 0;
    }
} 