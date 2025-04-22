package com.example.migym.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migym.models.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE id = 'current_user' LIMIT 1")
    LiveData<User> getUser();

    @Query("SELECT * FROM users WHERE id = 'current_user' LIMIT 1")
    User getUserSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users")
    void deleteAll();
} 