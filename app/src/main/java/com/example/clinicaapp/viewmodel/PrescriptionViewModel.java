package com.example.clinicaapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.data.repo.PrescriptionRepository;
import java.util.List;

public class PrescriptionViewModel extends AndroidViewModel {

    private final PrescriptionRepository repository;
    private final LiveData<List<Prescription>> allPrescriptions;

    public PrescriptionViewModel(@NonNull Application application) {
        super(application);
        repository = new PrescriptionRepository(application);
        allPrescriptions = repository.getAll();
    }

    public LiveData<List<Prescription>> getAllPrescriptions() { return allPrescriptions; }
    public LiveData<List<Prescription>> getByPatient(int patientId) { return repository.getByPatient(patientId); }
    public void insert(Prescription prescription) { repository.insert(prescription); }
    public void update(Prescription prescription) { repository.update(prescription); }
    public void delete(Prescription prescription) { repository.delete(prescription); }

    public LiveData<List<Prescription>> getAll() {
        return repository.getAll();
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public LiveData<Prescription> getById(int id) {
        return repository.getById(id);
    }

}
