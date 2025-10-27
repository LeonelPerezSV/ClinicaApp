package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.dao.PatientDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Patient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientRepository {
    private final PatientDao dao;
    private final ExecutorService executor;
    private final FirebaseSyncRepository sync;

    public PatientRepository(Context context) {
        dao = AppDatabase.getInstance(context).patientDao();
        executor = Executors.newSingleThreadExecutor();
        sync = new FirebaseSyncRepository(context);
    }

    public LiveData<List<Patient>> getAll() { return dao.getAll(); }

    public LiveData<Patient> getById(int id) { return dao.getById(id); }

    public void insert(Patient p) {
        executor.execute(() -> { dao.insert(p); sync.upsertPatient(p); });
    }

    public void update(Patient p) {
        executor.execute(() -> { dao.update(p); sync.upsertPatient(p); });
    }

    public void delete(Patient p) {
        executor.execute(() -> { dao.delete(p); sync.deletePatient(p.getId()); });
    }

    public void deleteById(int id) {
        executor.execute(() -> { dao.deleteById(id); sync.deletePatient(id); });
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void syncAll() {
        executor.execute(sync::pullPatientsDown);
    }
}
