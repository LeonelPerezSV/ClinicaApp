package com.example.clinicaapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.data.repo.MedicalRecordRepository;
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

    public LiveData<List<Patient>> getAllPatients() { return allPatients; }
    public void insert(Patient patient) { repository.insert(patient); }
    public void update(Patient patient) { repository.update(patient); }
    public void delete(Patient patient) { repository.delete(patient); }

    public LiveData<List<Patient>> getAll() {
        return repository.getAll();
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public LiveData<Patient> getById(int id) {
        return repository.getById(id);
    }

    // ✅ Eliminar paciente y expediente asociado
    public void deletePatientAndRecord(Patient patient) {
        new Thread(() -> {
            try {
                MedicalRecordRepository recordRepo = new MedicalRecordRepository(getApplication());
                List<MedicalRecord> records = recordRepo.getByPatient(patient.getId()).getValue();
                if (records != null) {
                    for (MedicalRecord r : records) {
                        recordRepo.delete(r);
                    }
                }
                repository.delete(patient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
