package com.example.clinicaapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.repo.DoctorRepository;
import java.util.List;

public class DoctorViewModel extends AndroidViewModel {

    private final DoctorRepository repository;
    private final LiveData<List<Doctor>> allDoctors;

    public DoctorViewModel(@NonNull Application application) {
        super(application);
        repository = new DoctorRepository(application);
        allDoctors = repository.getAll();
    }

    public LiveData<List<Doctor>> getAllDoctors() { return allDoctors; }
    public void insert(Doctor doctor) { repository.insert(doctor); }
    public void update(Doctor doctor) { repository.update(doctor); }
    public void delete(Doctor doctor) { repository.delete(doctor); }

    public LiveData<List<Doctor>> getAll() {
        return repository.getAll();
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public LiveData<Doctor> getById(int id) {
        return repository.getById(id);
    }

}
