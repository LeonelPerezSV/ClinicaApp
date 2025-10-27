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

    // 🔹 Todas las prescripciones
    public LiveData<List<Prescription>> getAll() {
        return dao.getAll();
    }

    // 🔹 Por paciente
    public LiveData<List<Prescription>> getByPatient(int patientId) {
        return dao.getByPatient(patientId);
    }

    // 🔹 Por ID
    public LiveData<Prescription> getById(int id) {
        // Corrige el método: el DAO tiene "findById"
        return dao.findById(id);
    }

    // 🔹 Insertar
    public void insert(Prescription p) {
        executor.execute(() -> {
            dao.insert(p);
            sync.upsertPrescription(p);
        });
    }

    // 🔹 Actualizar
    public void update(Prescription p) {
        executor.execute(() -> {
            dao.update(p);
            sync.upsertPrescription(p);
        });
    }

    // 🔹 Eliminar individual
    public void delete(Prescription p) {
        executor.execute(() -> {
            dao.delete(p);
            sync.deletePrescription(p.getId());
        });
    }

    // 🔹 Eliminar por ID
    public void deleteById(int id) {
        executor.execute(() -> {
            dao.deleteById(id);
            sync.deletePrescription(id);
        });
    }

    // 🔹 Eliminar todas
    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    // 🔹 Sincronizar desde Firestore
    public void syncAll() {
        executor.execute(sync::pullPrescriptionsDown);
    }
}
