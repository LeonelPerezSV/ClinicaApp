package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.example.clinicaapp.data.dao.MedicalRecordDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.MedicalRecord;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicalRecordRepository {

    private final MedicalRecordDao dao;
    private final ExecutorService executor;

    public MedicalRecordRepository(Context context) {
        dao = AppDatabase.getInstance(context).medicalRecordDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<MedicalRecord>> getAll() {
        return dao.getAll();
    }

    public void insert(MedicalRecord record) {
        executor.execute(() -> dao.insert(record));
    }

    public void insertAll(List<MedicalRecord> list) {
        executor.execute(() -> dao.insertAll(list));
    }

    public void update(MedicalRecord record) {
        executor.execute(() -> dao.update(record));
    }

    public void delete(MedicalRecord record) {
        executor.execute(() -> dao.delete(record));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public MedicalRecord findById(int id) {
        return dao.getById(id).getValue();
    }

    public LiveData<List<MedicalRecord>> getByPatient(int patientId) {
        return dao.getByPatient(patientId);
    }


    public void deleteById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }

    public LiveData<MedicalRecord> getById(int id) {
        return dao.getById(id);
    }
}
