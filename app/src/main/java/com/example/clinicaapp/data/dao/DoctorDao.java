package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.clinicaapp.data.entities.Doctor;
import java.util.List;

@Dao
public interface DoctorDao {

    // 🔹 Obtener todos los doctores
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    LiveData<List<Doctor>> getAll();

    // 🔹 Obtener doctor por ID (LiveData)
    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    LiveData<Doctor> getById(int id);

    // 🔹 Obtener doctor por ID (sin LiveData)
    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    Doctor findById(int id);

    // 🔹 Insertar un doctor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Doctor d);

    // 🔹 Insertar varios doctores
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Doctor> doctors);

    // 🔹 Actualizar un doctor
    @Update
    void update(Doctor d);

    // 🔹 Eliminar un doctor específico
    @Delete
    void delete(Doctor d);

    // 🔹 Eliminar todos los doctores
    @Query("DELETE FROM doctors")
    void deleteAll();

    // 🔹 Eliminar doctor por ID
    @Query("DELETE FROM doctors WHERE id = :id")
    void deleteById(int id);

    // 🔹 Obtener lista sincrónica (para dropdowns)
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    List<Doctor> getAllDoctorsList();
}
