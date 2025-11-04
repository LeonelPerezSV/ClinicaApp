package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.dao.AppointmentDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Appointment;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppointmentRepository {
    private final AppointmentDao dao;
    private final ExecutorService executor;
    private final FirebaseSyncRepository sync;

    public AppointmentRepository(Context context) {
        dao = AppDatabase.getInstance(context).appointmentDao();
        executor = Executors.newSingleThreadExecutor();
        sync = new FirebaseSyncRepository(context);
    }

    public LiveData<List<Appointment>> getAll() {
        return dao.getAll();
    }

    public LiveData<List<Appointment>> getByPatient(int patientId) {
        return dao.getByPatient(patientId);
    }

    public LiveData<Appointment> getById(int id) {
        return dao.getById(id);
    }

    public void insert(Appointment a) {
        executor.execute(() -> {
            dao.insert(a);
            sync.upsertAppointment(a);
        });
    }

    public void update(Appointment a) {
        executor.execute(() -> {
            dao.update(a);
            sync.upsertAppointment(a);
        });
    }

    public void delete(Appointment a) {
        executor.execute(() -> {
            dao.delete(a);
            sync.deleteAppointment(a.getId());
        });
    }

    public void deleteById(int id) {
        executor.execute(() -> {
            dao.deleteById(id);
            sync.deleteAppointment(id);
        });
    }


     //Borra todas las citas de un paciente específico, tanto localmente como en Firestore.
     //@param patientId El ID del paciente cuyas citas se eliminarán.

    public void deleteByPatientId(int patientId) {
        executor.execute(() -> {
            List<Appointment> appointmentsToDelete = dao.getAllSyncByPatient(patientId);
            for (Appointment app : appointmentsToDelete) {
                sync.deleteAppointment(app.getId());
            }
            dao.deleteByPatientId(patientId);
        });
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void syncAll() {
        executor.execute(sync::pullAppointmentsDown);
    }
}