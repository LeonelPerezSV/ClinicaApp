package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clinicaapp.data.entities.MedicalRecord;
import java.util.List;

@Dao
public interface MedicalRecordDao {

    //los métodos de inserción y actualización devuelve el ID (long) del expediente insertado.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MedicalRecord record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MedicalRecord> records);

    @Update
    void update(MedicalRecord record);

    @Delete
    void delete(MedicalRecord record);

    @Query("DELETE FROM medical_records WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM medical_records")
    void deleteAll();

    @Query("SELECT * FROM medical_records ORDER BY id DESC")
    LiveData<List<MedicalRecord>> getAll();

    @Query("SELECT * FROM medical_records WHERE id = :id LIMIT 1")
    LiveData<MedicalRecord> getById(int id);

    @Query("SELECT * FROM medical_records WHERE patientId = :patientId LIMIT 1")
    LiveData<MedicalRecord> getRecordByPatientId(int patientId);

    @Query("SELECT * FROM medical_records WHERE patientId = :patientId")
    List<MedicalRecord> getAllSyncByPatient(int patientId);

    @Query("DELETE FROM medical_records WHERE patientId = :patientId")
    void deleteByPatientId(int patientId);
}
