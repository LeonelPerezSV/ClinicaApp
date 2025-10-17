package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clinicaapp.data.entities.User;
import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);  // 🔹 devuelve el ID generado


    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("SELECT * FROM users ORDER BY username ASC")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findById(int id);


    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getById(int id);


}



