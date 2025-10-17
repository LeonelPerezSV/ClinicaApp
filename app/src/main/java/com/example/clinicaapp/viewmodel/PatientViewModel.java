package com.example.clinicaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.repo.PatientRepository;

import java.util.List;

public class PatientViewModel extends AndroidViewModel {

    private final PatientRepository repository;
    private final LiveData<List<Patient>> allPatients;

    public PatientViewModel(@NonNull Application application) {
        super(application);
        repository = new PatientRepository(application);
        allPatients = repository.getAll();
    }

    public LiveData<List<Patient>> getAll() {
        return allPatients;
    }

    public LiveData<Patient> getById(int id) {
        return repository.getById(id);
    }

    public void insert(Patient patient) {
        repository.insert(patient);
    }

    public void update(Patient patient) {
        repository.update(patient);
    }

    public void delete(Patient patient) {
        repository.delete(patient);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    // 🔹 Eliminar paciente con cascada (citas, recetas, expediente)
    public void deletePatientCascade(Patient patient) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplication());
            int patientId = patient.getId();
            db.prescriptionDao().deleteByPatientId(patientId);
            db.appointmentDao().deleteByPatientId(patientId);
            db.medicalRecordDao().deleteByPatientId(patientId);
            db.patientDao().delete(patient);
        }).start();
    }
}
