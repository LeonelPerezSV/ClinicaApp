package com.example.clinicaapp.ui.appointments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnAppointmentClick { void onClick(Appointment item); }
    private final OnAppointmentClick listener;

    //El adaptador ahora almacena las 3 listas de datos en sus propios atributos.
    private final List<Appointment> appointmentList = new ArrayList<>();
    private final List<Patient> patientList = new ArrayList<>();
    private final List<Doctor> doctorList = new ArrayList<>();

    public AppointmentAdapter(OnAppointmentClick listener) { this.listener = listener; }

     //Método para actualizar todos los datos del adaptador con los nuevos datos.
     //@param appointments La lista de citas .
     //@param patients La lista de todos los pacientes.
     //@param doctors La lista de todos los doctores.

    public void setData(List<Appointment> appointments, List<Patient> patients, List<Doctor> doctors) {
        this.appointmentList.clear();
        this.patientList.clear();
        this.doctorList.clear();

        if (appointments != null) this.appointmentList.addAll(appointments);
        if (patients != null) this.patientList.addAll(patients);
        if (doctors != null) this.doctorList.addAll(doctors);

        notifyDataSetChanged();
    }

    public Appointment getAt(int pos) { return appointmentList.get(pos); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Appointment a = appointmentList.get(pos);

        // Buscar el nombre del paciente y del doctor usando los IDs.
        String patientName = findPatientName(a.getPatientId());
        String doctorName = findDoctorName(a.getDoctorId());

        ((TextView) h.itemView.findViewById(android.R.id.text1)).setText("Cita " + a.getDate() + " " + a.getTime() + " · " + a.getStatus());
        ((TextView) h.itemView.findViewById(android.R.id.text2)).setText("Paciente: " + patientName + " · Doctor: " + doctorName);
        h.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override public int getItemCount() { return appointmentList.size(); }

    // --- Métodos de Ayuda para buscar nombres ---

    private String findPatientName(int patientId) {
        Optional<Patient> patient = patientList.stream()
                .filter(p -> p.getId() == patientId)
                .findFirst();
        return patient.isPresent() ? patient.get().getName() : "ID: " + patientId; // Devuelve el nombre o el ID si no se encuentra
    }

    private String findDoctorName(int doctorId) {
        Optional<Doctor> doctor = doctorList.stream()
                .filter(d -> d.getId() == doctorId)
                .findFirst();
        return doctor.isPresent() ? doctor.get().getName() : "ID: " + doctorId;
    }

    static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
}
