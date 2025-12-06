package com.example.clinicaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.repo.AppointmentRepository;
import com.example.clinicaapp.data.repo.MedicalRecordRepository;
import com.example.clinicaapp.data.repo.PatientRepository;
import com.example.clinicaapp.data.repo.PrescriptionRepository;

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

    //Método para obtener el ID de paciente a partir del ID de usuario.
     //Necesario para que un usuario de tipo 'Paciente' pueda ver su propia lista de citas.
     //@param userId El ID del usuario logueado.
     //@return Un LiveData que emitirá el ID del paciente correspondiente.

    public LiveData<Integer> getPatientIdByUserId(int userId) {
        return repository.getPatientIdByUserId(userId);
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

    public void deletePatientCascade(Patient patient) {
        new Thread(() -> {
            AppointmentRepository appointmentRepo = new AppointmentRepository(getApplication());
            PrescriptionRepository prescriptionRepo = new PrescriptionRepository(getApplication());
            MedicalRecordRepository medicalRecordRepo = new MedicalRecordRepository(getApplication());

            int patientId = patient.getId();

            appointmentRepo.deleteByPatientId(patientId);
            prescriptionRepo.deleteByPatientId(patientId);
            medicalRecordRepo.deleteByPatientId(patientId);

            repository.delete(patient);
        }).start();
    }
}
