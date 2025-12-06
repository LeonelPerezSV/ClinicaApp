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
    private final FirebaseSyncRepository sync;

    public PrescriptionRepository(Context context) {
        dao = AppDatabase.getInstance(context).prescriptionDao();
        executor = Executors.newSingleThreadExecutor();
        sync = new FirebaseSyncRepository(context);
    }

    public LiveData<List<Prescription>> getAll() { return dao.getAll(); }

    public LiveData<List<Prescription>> getByPatient(int patientId) { return dao.getByPatient(patientId); }

    public LiveData<Prescription> getById(int id) { return dao.findById(id); }

    //Lógica de inserción para garantizar la consistencia de IDs.
    public void insert(Prescription p) {
        executor.execute(() -> {
            long newId = dao.insert(p);
            p.setId((int) newId);
            sync.upsertPrescription(p);
        });
    }

    public void update(Prescription p) {
        executor.execute(() -> {
            dao.update(p);
            sync.upsertPrescription(p);
        });
    }

    public void delete(Prescription p) {
        executor.execute(() -> {
            dao.delete(p);
            sync.deletePrescription(p.getId());
        });
    }

    public void deleteById(int id) {
        executor.execute(() -> {
            dao.deleteById(id);
            sync.deletePrescription(id);
        });
    }

    public void deleteByPatientId(int patientId) {
        executor.execute(() -> {
            List<Prescription> prescriptionsToDelete = dao.getAllSyncByPatient(patientId);
            for (Prescription p : prescriptionsToDelete) {
                sync.deletePrescription(p.getId());
            }
            dao.deleteByPatientId(patientId);
        });
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void syncAll() {
        executor.execute(sync::pullPrescriptionsDown);
    }
}
