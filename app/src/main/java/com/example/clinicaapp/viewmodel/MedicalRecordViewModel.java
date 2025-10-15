package com.example.clinicaapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.data.repo.MedicalRecordRepository;
import java.util.List;

public class MedicalRecordViewModel extends AndroidViewModel {

    private final MedicalRecordRepository repository;
    private final LiveData<List<MedicalRecord>> allRecords;

    public MedicalRecordViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicalRecordRepository(application);
        allRecords = repository.getAll();
    }

    public LiveData<List<MedicalRecord>> getAllRecords() { return allRecords; }
    public LiveData<List<MedicalRecord>> getByPatient(int patientId) { return repository.getByPatient(patientId); }
    public void insert(MedicalRecord record) { repository.insert(record); }
    public void update(MedicalRecord record) { repository.update(record); }
    public void delete(MedicalRecord record) { repository.delete(record); }

    public LiveData<List<MedicalRecord>> getAll() {
        return repository.getAll();
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }
    public LiveData<MedicalRecord> getById(int id) {
        return repository.getById(id);
    }
}
