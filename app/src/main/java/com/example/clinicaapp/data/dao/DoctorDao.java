package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.clinicaapp.data.entities.Doctor;
import java.util.List;

@Dao
public interface DoctorDao {

    @Query("SELECT * FROM doctor ORDER BY lastName ASC")
    LiveData<List<Doctor>> getAll();

    @Query("SELECT * FROM doctor WHERE id = :id LIMIT 1")
    LiveData<Doctor> getById(int id);

    @Query("SELECT * FROM doctor WHERE id = :id LIMIT 1")
    Doctor findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Doctor d);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Doctor> doctors);

    @Update
    void update(Doctor d);

    @Delete
    void delete(Doctor d);

    @Query("DELETE FROM doctor")
    void deleteAll();

    @Query("DELETE FROM doctor WHERE id = :id")
    void deleteById(int id);
}
