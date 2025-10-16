package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clinicaapp.data.entities.Prescription;
import java.util.List;

@Dao
public interface PrescriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Prescription prescription);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Prescription> prescriptions);

    @Update
    void update(Prescription prescription);

    @Delete
    void delete(Prescription prescription);

    @Query("DELETE FROM prescriptions")
    void deleteAll();

    @Query("SELECT * FROM prescriptions ORDER BY date DESC")
    LiveData<List<Prescription>> getAll();


    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    LiveData<Prescription> findById(int id);


    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId ORDER BY date DESC")
    LiveData<List<Prescription>> getByPatient(int patientId);


    @Query("DELETE FROM prescriptions WHERE id = :id")
    void deleteById(int id);
}
