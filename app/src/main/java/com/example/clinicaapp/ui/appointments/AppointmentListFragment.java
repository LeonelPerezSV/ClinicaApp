package com.example.clinicaapp.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.databinding.FragmentAppointmentListBinding;
import java.util.List;
import java.util.concurrent.Executors;

public class AppointmentListFragment extends Fragment {

    private FragmentAppointmentListBinding binding;
    private boolean isDoctor;
    private long userId;
    private AppointmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppointmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String type = prefs.getString("user_type", "Paciente");
        userId = prefs.getLong("user_id", 0);
        isDoctor = "Doctor".equalsIgnoreCase(type);

        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AppointmentAdapter(a -> {
            // Abrir en modo edición
            Bundle b = new Bundle();
            b.putInt("arg_id", a.getId()); // 👈 usamos la misma key que en newInstance/ARG_ID
            androidx.navigation.Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_nav_citas_to_form, b);
        });
        binding.rvAppointments.setAdapter(adapter);

        // FAB sólo doctor
        binding.fabAdd.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        binding.fabAdd.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v)
                        .navigate(R.id.action_nav_citas_to_form));

        cargarCitas();
    }

    @Override public void onResume() {
        super.onResume();
        cargarCitas(); // refrescar al volver del form
    }

    private void cargarCitas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Appointment> list = isDoctor
                    ? db.appointmentDao().getAppointmentsForDoctor(userId)
                    : db.appointmentDao().getAppointmentsForPatient(userId);

            requireActivity().runOnUiThread(() -> {
                binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.submit(list);
            });
        });
    }
}
