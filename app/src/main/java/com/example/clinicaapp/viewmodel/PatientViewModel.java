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


     //Elimina el paciente y todos sus datos asociados: citas, recetas, expediente
     //utilizando los repositorios correspondientes para asegurar la sincronización con Firestore.
     //@param patient El paciente a eliminar.
    public void deletePatientCascade(Patient patient) {
        new Thread(() -> {
            // Se instancian los repositorios necesarios para el borrado en cascada.
            AppointmentRepository appointmentRepo = new AppointmentRepository(getApplication());
            PrescriptionRepository prescriptionRepo = new PrescriptionRepository(getApplication());
            MedicalRecordRepository medicalRecordRepo = new MedicalRecordRepository(getApplication());

            int patientId = patient.getId();

            //  Borrar datos asociados a través de sus repositorios.
            //    Esto asegura que si esos repositorios tienen lógica de sincronización, se ejecute.
            appointmentRepo.deleteByPatientId(patientId);
            prescriptionRepo.deleteByPatientId(patientId);
            medicalRecordRepo.deleteByPatientId(patientId);

            // Borrar el paciente a través de su propio repositorio.
            // Esto garantiza que se llame a FirebaseSyncRepository para borrarlo de la nube.
            repository.delete(patient);
            
        }).start();
    }
}
