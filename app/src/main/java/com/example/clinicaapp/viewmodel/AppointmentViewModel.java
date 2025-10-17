package com.example.clinicaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.repo.AppointmentRepository;

import java.util.List;

public class AppointmentViewModel extends AndroidViewModel {

    private final AppointmentRepository repository;
    private final LiveData<List<Appointment>> allAppointments;

    public AppointmentViewModel(@NonNull Application application) {
        super(application);
        repository = new AppointmentRepository(application);
        allAppointments = repository.getAll();
    }

    public LiveData<List<Appointment>> getAllAppointments() {
        return allAppointments;
    }

    public LiveData<List<Appointment>> getAppointmentsByPatient(int patientId) {
        return repository.getByPatient(patientId);
    }

    public LiveData<Appointment> getById(int id) {
        return repository.getById(id);
    }

    public void insert(Appointment appointment) {
        repository.insert(appointment);
    }

    public void update(Appointment appointment) {
        repository.update(appointment);
    }

    public void delete(Appointment appointment) {
        repository.delete(appointment);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
