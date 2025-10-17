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

    // 🔹 Obtener todas las recetas (doctor)
    public LiveData<List<Prescription>> getAllPrescriptions() {
        return allPrescriptions;
    }

    // 🔹 Obtener recetas por paciente
    public LiveData<List<Prescription>> getPrescriptionsByPatient(int patientId) {
        return repository.getByPatient(patientId);
    }

    // 🔹 Obtener receta por ID
    public LiveData<Prescription> getById(int id) {
        return repository.getById(id);
    }

    // 🔹 Insertar receta
    public void insert(Prescription prescription) {
        repository.insert(prescription);
    }

    // 🔹 Actualizar receta
    public void update(Prescription prescription) {
        repository.update(prescription);
    }

    // 🔹 Eliminar receta
    public void delete(Prescription prescription) {
        repository.delete(prescription);
    }

    // 🔹 Eliminar receta por ID
    public void deleteById(int id) {
        repository.deleteById(id);
    }

    // 🔹 Eliminar todas las recetas
    public void deleteAll() {
        repository.deleteAll();
    }
}
