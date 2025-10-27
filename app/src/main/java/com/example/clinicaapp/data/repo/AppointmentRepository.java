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

    // 🔹 Obtener todas las citas
    public LiveData<List<Appointment>> getAll() {
        return dao.getAll();
    }

    // 🔹 Obtener citas por paciente
    public LiveData<List<Appointment>> getByPatient(int patientId) {
        return dao.getByPatient(patientId);
    }

    // 🔹 Obtener cita por ID
    public LiveData<Appointment> getById(int id) {
        return dao.getById(id);
    }

    // 🔹 Insertar cita
    public void insert(Appointment a) {
        executor.execute(() -> {
            dao.insert(a);
            sync.upsertAppointment(a);
        });
    }

    // 🔹 Actualizar cita
    public void update(Appointment a) {
        executor.execute(() -> {
            dao.update(a);
            sync.upsertAppointment(a);
        });
    }

    // 🔹 Eliminar cita individual
    public void delete(Appointment a) {
        executor.execute(() -> {
            dao.delete(a);
            sync.deleteAppointment(a.getId());
        });
    }

    // 🔹 Eliminar cita por ID
    public void deleteById(int id) {
        executor.execute(() -> {
            dao.deleteById(id);
            sync.deleteAppointment(id);
        });
    }

    // 🔹 Eliminar todas las citas
    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    // 🔹 Sincronizar todas las citas (pull desde Firestore)
    public void syncAll() {
        executor.execute(sync::pullAppointmentsDown);
    }
}
