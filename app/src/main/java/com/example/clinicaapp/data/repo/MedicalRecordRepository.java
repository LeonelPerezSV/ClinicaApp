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
    private final FirebaseSyncRepository sync;

    public MedicalRecordRepository(Context context) {
        dao = AppDatabase.getInstance(context).medicalRecordDao();
        executor = Executors.newSingleThreadExecutor();
        sync = new FirebaseSyncRepository(context);
    }

    public LiveData<List<MedicalRecord>> getAll() { return dao.getAll(); }

    public LiveData<List<MedicalRecord>> getByPatient(int patientId) { return dao.getByPatient(patientId); }

    public LiveData<MedicalRecord> getById(int id) { return dao.getById(id); }

    public void insert(MedicalRecord r) {
        executor.execute(() -> { dao.insert(r); sync.upsertRecord(r); });
    }

    public void update(MedicalRecord r) {
        executor.execute(() -> { dao.update(r); sync.upsertRecord(r); });
    }

    public void delete(MedicalRecord r) {
        executor.execute(() -> { dao.delete(r); sync.deleteRecord(r.getId()); });
    }

    public void deleteById(int id) {
        executor.execute(() -> { dao.deleteById(id); sync.deleteRecord(id); });
    }

    //Borra todos los expedientes de un paciente específico, tanto localmente como en Firestore.
    //@param patientId El ID del paciente cuyos expedientes se eliminarán.

    public void deleteByPatientId(int patientId) {
        executor.execute(() -> {
            List<MedicalRecord> recordsToDelete = dao.getAllSyncByPatient(patientId);
            for (MedicalRecord rec : recordsToDelete) {
                sync.deleteRecord(rec.getId());
            }
            dao.deleteByPatientId(patientId);
        });
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void syncAll() {
        executor.execute(sync::pullRecordsDown);
    }
}
