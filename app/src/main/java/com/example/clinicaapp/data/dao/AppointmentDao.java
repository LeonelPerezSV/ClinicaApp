package com.example.clinicaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clinicaapp.data.entities.Appointment;
import java.util.List;

@Dao
public interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Appointment appointment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Appointment> appointments);

    @Update
    void update(Appointment appointment);

    @Delete
    void delete(Appointment appointment);

    @Query("DELETE FROM appointments")
    void deleteAll();

    @Query("DELETE FROM appointments WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    LiveData<List<Appointment>> getAll();

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    Appointment findById(int id);

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    LiveData<Appointment> getById(int id);

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY date DESC")
    List<Appointment> getAppointmentsForDoctor(long doctorId);

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY date DESC")
    List<Appointment> getAppointmentsForPatient(long patientId);

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY date DESC")
    LiveData<List<Appointment>> getByPatient(int patientId);

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    Appointment getByIdDirect(int id);

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND date = date('now') ORDER BY time ASC")
    List<Appointment> findTodayAppointmentsForDoctor(long doctorId);

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY date, time LIMIT 1")
    Appointment findNextAppointmentForPatient(long patientId);

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY date DESC")
    LiveData<List<Appointment>> getByDoctor(int doctorId);
}
