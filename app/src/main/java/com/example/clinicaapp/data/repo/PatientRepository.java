package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.example.clinicaapp.data.dao.PatientDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.data.entities.Patient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientRepository {

    private final PatientDao dao;
    private final ExecutorService executor;

    public PatientRepository(Context context) {
        dao = AppDatabase.getInstance(context).patientDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Patient>> getAll() {
        return dao.getAll();
    }

    public void insert(Patient patient) {
        executor.execute(() -> dao.insert(patient));
    }

    public void insertAll(List<Patient> patients) {
        executor.execute(() -> dao.insertAll(patients));
    }

    public void update(Patient patient) {
        executor.execute(() -> dao.update(patient));
    }

    public void delete(Patient patient) {
        executor.execute(() -> dao.delete(patient));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public Patient findById(int id) {
        return dao.findById(id);
    }

    public void deleteById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }


    public LiveData<Patient> getById(int id) {
        return dao.getById(id);
    }


}
