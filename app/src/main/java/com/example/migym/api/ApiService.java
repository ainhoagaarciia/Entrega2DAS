package com.example.migym.api;

import com.example.migym.models.LoginUser;
import com.example.migym.models.User;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("auth/register")
    Call<User> register(@Body User user);

    @POST("auth/login")
    Call<User> login(@Body LoginUser loginUser);

    @Multipart
    @POST("users/profile-image")
    Call<User> uploadProfileImage(@Part MultipartBody.Part image);

    @PUT("users/location")
    Call<User> updateLocation(@Body User user);
} 