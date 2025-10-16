package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.example.clinicaapp.data.dao.DoctorDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Doctor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorRepository {

    private final DoctorDao dao;
    private final ExecutorService executor;

    public DoctorRepository(Context context) {
        dao = AppDatabase.getInstance(context).doctorDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Doctor>> getAll() {
        return dao.getAll();
    }

    public void insert(Doctor doctor) {
        executor.execute(() -> dao.insert(doctor));
    }

    public void insertAll(List<Doctor> doctors) {
        executor.execute(() -> dao.insertAll(doctors));
    }

    public void update(Doctor doctor) {
        executor.execute(() -> dao.update(doctor));
    }

    public void delete(Doctor doctor) {
        executor.execute(() -> dao.delete(doctor));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public Doctor findById(int id) {
        return dao.findById(id);
    }

    public void deleteById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }

    public LiveData<Doctor> getById(int id) {
        return dao.getById(id);
    }

    // 🔹 Obtener todos los doctores de forma sincrónica (sin LiveData)
    public List<Doctor> getAllList() {
        return dao.getAllDoctorsList();
    }
}
