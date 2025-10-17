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

    public AppointmentRepository(Context context) {
        dao = AppDatabase.getInstance(context).appointmentDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Appointment>> getAll() {
        return dao.getAll();
    }

    public void insert(Appointment appointment) {
        executor.execute(() -> dao.insert(appointment));
    }

    public void insertAll(List<Appointment> appointments) {
        executor.execute(() -> dao.insertAll(appointments));
    }

    public void update(Appointment appointment) {
        executor.execute(() -> dao.update(appointment));
    }

    public void delete(Appointment appointment) {
        executor.execute(() -> dao.delete(appointment));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public Appointment findById(int id) {
        return dao.findById(id);
    }

    public LiveData<List<Appointment>> getByPatient(int patientId) {
        return dao.getByPatient(patientId);
    }


    public LiveData<Appointment> getById(int id) {
        return dao.getById(id);
    }


    public void deleteById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }


    public LiveData<List<Appointment>> getByDoctorLive(int doctorId) {
        // Ajustamos a la versión existente del DAO
        return new androidx.lifecycle.MutableLiveData<>(
                dao.getAppointmentsForDoctor(doctorId)
        );
    }
    public void syncAll() {
        // Placeholder: lógica de sincronización si tuvieras API.
    }

    public List<Appointment> getAllSyncByPatient(int patientId) {
        return dao.getAllSyncByPatient(patientId);
    }

    public void deleteByPatientIdSync(int patientId) {
        dao.deleteByPatientId(patientId);
    }

    public void deleteSync(Appointment a) { dao.delete(a); }


}
