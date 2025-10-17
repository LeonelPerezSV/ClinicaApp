package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.entities.User;

import java.util.List;

@Dao
public interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Patient patient);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Patient> patients);

    @Update
    void update(Patient patient);

    @Delete
    void delete(Patient patient);

    @Query("DELETE FROM patients")
    void deleteAll();

    @Query("SELECT * FROM patients ORDER BY lastName ASC")
    LiveData<List<Patient>> getAll();

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    Patient findById(int id);

    @Query("DELETE FROM patients WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    LiveData<Patient> getById(int id);

    @Query("SELECT * FROM patients ORDER BY lastName ASC")
    List<Patient> getAllPatientsList();

    @Query("SELECT id FROM patients WHERE userId = :userId LIMIT 1")
    int getPatientIdByUserId(int userId);



}
