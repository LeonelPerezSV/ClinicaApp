package com.example.clinicaapp.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.dao.AppointmentDao;
import com.example.clinicaapp.data.dao.DoctorDao;
import com.example.clinicaapp.data.dao.PatientDao;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Usuario");
        String userType = prefs.getString("user_type", "Desconocido");
        int userId = (int) prefs.getLong("user_id", 0L);

        binding.tvWelcome.setText("Bienvenido a ClínicaApp, " + userName);
        binding.tvUserType.setText("Tipo de usuario: " + userType);

        if ("Paciente".equalsIgnoreCase(userType)) {
            binding.layoutPaciente.setVisibility(View.VISIBLE);
            binding.layoutDoctor.setVisibility(View.GONE);
            mostrarCitaPaciente(userId);
        } else if ("Doctor".equalsIgnoreCase(userType)) {
            binding.layoutDoctor.setVisibility(View.VISIBLE);
            binding.layoutPaciente.setVisibility(View.GONE);
            mostrarTodasLasCitas();
        }

        return root;
    }

    private void mostrarCitaPaciente(int userId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Patient patient = db.patientDao().findByUserId(userId);

            if (patient != null) {
                Appointment cita = db.appointmentDao().findNextAppointmentForPatient(patient.getId());
                final String infoText;
                if (cita != null) {
                    Doctor doctor = db.doctorDao().findById(cita.getDoctorId());
                    String doctorName = (doctor != null) ? doctor.getName() : "ID: " + cita.getDoctorId();
                    infoText = "📅 " + cita.getDate() + " a las " + cita.getTime() +
                            "\n👨‍⚕️ Doctor: " + doctorName;
                } else {
                    infoText = "No tienes citas programadas.";
                }
                requireActivity().runOnUiThread(() -> binding.tvCitaPaciente.setText(infoText));
            } else {
                requireActivity().runOnUiThread(() -> binding.tvCitaPaciente.setText("No se encontró perfil de paciente."));
            }
        }).start();
    }

    private void mostrarTodasLasCitas() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Appointment> todasLasCitas = db.appointmentDao().getAllSync();
            List<Patient> todosLosPacientes = db.patientDao().getAllPatientsList();
            List<Doctor> todosLosDoctores = db.doctorDao().getAllDoctorsList();

            requireActivity().runOnUiThread(() -> {
                if (todasLasCitas.isEmpty()) {
                    binding.tvDoctorEmptyState.setVisibility(View.VISIBLE);
                    binding.rvCitasDoctor.setVisibility(View.GONE);
                } else {
                    binding.tvDoctorEmptyState.setVisibility(View.GONE);
                    binding.rvCitasDoctor.setVisibility(View.VISIBLE);
                    binding.rvCitasDoctor.setLayoutManager(new LinearLayoutManager(requireContext()));
                    binding.rvCitasDoctor.setAdapter(new HomeCitasDoctorAdapter(todasLasCitas, todosLosPacientes, todosLosDoctores));
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
