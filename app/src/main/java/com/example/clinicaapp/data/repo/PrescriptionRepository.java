package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.dao.PrescriptionDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Prescription;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrescriptionRepository {

    private final PrescriptionDao dao;
    private final ExecutorService executor;

    public PrescriptionRepository(Context context) {
        dao = AppDatabase.getInstance(context).prescriptionDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Prescription>> getAll() {
        return dao.getAll();
    }

    public void insert(Prescription prescription) {
        executor.execute(() -> dao.insert(prescription));
    }

    public void insertAll(List<Prescription> list) {
        executor.execute(() -> dao.insertAll(list));
    }

    public void update(Prescription prescription) {
        executor.execute(() -> dao.update(prescription));
    }

    public void delete(Prescription prescription) {
        executor.execute(() -> dao.delete(prescription));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public LiveData<List<Prescription>> getByPatient(int patientId) {
        return dao.getByPatient(patientId);
    }

    public void deleteById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }

    public LiveData<Prescription> getById(int id) {
        return dao.findById(id);
    }
}
