package com.example.clinicaapp.ui.citas;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.repo.AppointmentRepository;
import java.util.List;

public class CitasViewModel extends AndroidViewModel {

    private final AppointmentRepository repo;
    private LiveData<List<Appointment>> citas;

    public CitasViewModel(@NonNull Application application) {
        super(application);
        repo = new AppointmentRepository(application);
        citas = repo.getAll(); // ✅ corregido
    }

    public LiveData<List<Appointment>> getCitas() {
        return citas;
    }

    public void observeByPatient(int patientId) {
        citas = repo.getByPatient(patientId); // ✅ corregido
    }

    public void sync() {
        repo.syncAll(); // sigue válido
    }
}
