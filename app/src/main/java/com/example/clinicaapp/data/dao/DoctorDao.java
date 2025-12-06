package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.clinicaapp.data.entities.Doctor;
import java.util.List;

@Dao
public interface DoctorDao {

    @Query("SELECT * FROM doctors ORDER BY name ASC")
    LiveData<List<Doctor>> getAll();

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    LiveData<Doctor> getById(int id);

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    Doctor findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Doctor d);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Doctor> doctors);

    @Update
    void update(Doctor d);

    @Delete
    void delete(Doctor d);

    @Query("DELETE FROM doctors")
    void deleteAll();

    @Query("DELETE FROM doctors WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM doctors ORDER BY name ASC")
    List<Doctor> getAllDoctorsList();

    @Query("SELECT * FROM doctors WHERE userId = :userId LIMIT 1")
    Doctor findByUserId(int userId);

    @Query("SELECT * FROM doctors WHERE email = :email LIMIT 1")
    Doctor findByEmail(String email);
}
