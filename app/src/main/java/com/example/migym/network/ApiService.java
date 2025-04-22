package com.example.migym.network;

import com.example.migym.models.Workout;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface ApiService {
    @GET("workouts")
    Call<List<Workout>> getWorkouts();

    @POST("workouts")
    Call<Workout> createWorkout(@Body Workout workout);

    @DELETE("workouts/{id}")
    Call<Void> deleteWorkout(@Path("id") String workoutId);
} 