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
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.databinding.FragmentHomeBinding;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Obtener datos del usuario logueado
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Usuario");
        String userType = prefs.getString("user_type", "Desconocido");
        long userId = prefs.getLong("user_id", 0L);

        binding.tvWelcome.setText("Bienvenido a ClínicaApp, " + userName);
        binding.tvUserType.setText("Tipo de usuario: " + userType);

        // Mostrar layout según tipo de usuario
        if ("Paciente".equalsIgnoreCase(userType)) {
            binding.layoutPaciente.setVisibility(View.VISIBLE);
            binding.layoutDoctor.setVisibility(View.GONE);
            mostrarCitaPaciente(userId);
        } else if ("Doctor".equalsIgnoreCase(userType)) {
            binding.layoutDoctor.setVisibility(View.VISIBLE);
            binding.layoutPaciente.setVisibility(View.GONE);
            mostrarCitasDoctor(userId);
        }

        return root;
    }

    private void mostrarCitaPaciente(long userId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            AppointmentDao dao = db.appointmentDao();
            Appointment cita = dao.findNextAppointmentForPatient(userId);
            requireActivity().runOnUiThread(() -> {
                if (cita != null) {
                    String info = "📅 " + cita.getDate() + " a las " + cita.getTime() +
                            "\n👨‍⚕️ Doctor ID: " + cita.getDoctorId();
                    binding.tvCitaPaciente.setText(info);
                } else {
                    binding.tvCitaPaciente.setText("No tienes citas programadas.");
                }
            });
        }).start();
    }

    private void mostrarCitasDoctor(long userId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            AppointmentDao dao = db.appointmentDao();
            List<Appointment> citasHoy = dao.findTodayAppointmentsForDoctor(userId);

            requireActivity().runOnUiThread(() -> {
                binding.rvCitasDoctor.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.rvCitasDoctor.setAdapter(new HomeCitasDoctorAdapter(citasHoy));
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
