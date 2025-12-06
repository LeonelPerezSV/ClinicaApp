package com.example.clinicaapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinicaapp.R;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.Patient;

import java.util.List;
import java.util.Optional;

public class HomeCitasDoctorAdapter extends RecyclerView.Adapter<HomeCitasDoctorAdapter.ViewHolder> {

    private final List<Appointment> citas;
    private final List<Patient> patients;
    private final List<Doctor> doctors;

    public HomeCitasDoctorAdapter(List<Appointment> citas, List<Patient> patients, List<Doctor> doctors) {
        this.citas = citas;
        this.patients = patients;
        this.doctors = doctors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita_doctor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment c = citas.get(position);

        String patientName = findPatientName(c.getPatientId());
        String doctorName = findDoctorName(c.getDoctorId());

        holder.tvFecha.setText("📅 " + c.getDate());
        holder.tvPaciente.setText("👤 Paciente: " + patientName);
        holder.tvDoctor.setText("👨‍⚕️ Doctor: " + doctorName);
        holder.tvHora.setText("🕒 " + c.getTime());
        holder.tvEstado.setText("Estado: " + c.getStatus());
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    private String findPatientName(int patientId) {
        if (patients == null) return "ID: " + patientId;
        Optional<Patient> patient = patients.stream()
                .filter(p -> p.getId() == patientId)
                .findFirst();
        return patient.isPresent() ? patient.get().getName() : "ID: " + patientId;
    }

    private String findDoctorName(int doctorId) {
        if (doctors == null) return "ID: " + doctorId;
        Optional<Doctor> doctor = doctors.stream()
                .filter(d -> d.getId() == doctorId)
                .findFirst();
        return doctor.isPresent() ? doctor.get().getName() : "ID: " + doctorId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvPaciente, tvDoctor, tvHora, tvEstado;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
