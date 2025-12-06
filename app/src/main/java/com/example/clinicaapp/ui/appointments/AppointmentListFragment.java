package com.example.clinicaapp.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinicaapp.R;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.databinding.FragmentAppointmentListBinding;
import com.example.clinicaapp.viewmodel.AppointmentViewModel;
import com.example.clinicaapp.viewmodel.DoctorViewModel;
import com.example.clinicaapp.viewmodel.PatientViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AppointmentListFragment extends Fragment implements AppointmentAdapter.OnAppointmentClick {

    private FragmentAppointmentListBinding binding;
    private AppointmentViewModel appointmentViewModel;
    private PatientViewModel patientViewModel;
    private DoctorViewModel doctorViewModel;
    private AppointmentAdapter adapter;

    private boolean isDoctor;
    private int userId;

    private List<Appointment> appointmentList;
    private List<Patient> patientList;
    private List<Doctor> doctorList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppointmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String type = prefs.getString("user_type", "Paciente");
        userId = (int) prefs.getLong("user_id", 0L);
        isDoctor = "Doctor".equalsIgnoreCase(type);

        ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity());
        appointmentViewModel = viewModelProvider.get(AppointmentViewModel.class);
        patientViewModel = viewModelProvider.get(PatientViewModel.class);
        doctorViewModel = viewModelProvider.get(DoctorViewModel.class);

        adapter = new AppointmentAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        setupObservers();
        setupUI();
    }

    private void setupObservers() {
        // Lógica de carga secuencial para evitar race conditions.
        // 1. Cargar las listas de soporte (Doctores y Pacientes).
        doctorViewModel.getAll().observe(getViewLifecycleOwner(), doctors -> {
            this.doctorList = doctors;
            // Una vez que los doctores están cargados, intentamos cargar las citas.
            loadPrimaryData();
        });

        patientViewModel.getAll().observe(getViewLifecycleOwner(), patients -> {
            this.patientList = patients;
            // Una vez que los pacientes están cargados, intentamos cargar las citas.
            loadPrimaryData();
        });
    }

    private void loadPrimaryData() {
        // No hacer nada hasta que ambas listas de soporte estén listas.
        if (patientList == null || doctorList == null) {
            return;
        }

        // tenemos las listas de nombres, cargamos la lista principal (Citas).
        if (isDoctor) {
            appointmentViewModel.getAllAppointments().observe(getViewLifecycleOwner(), appointments -> {
                this.appointmentList = appointments;
                tryUpdateAdapter();
            });
        } else {
            patientViewModel.getPatientIdByUserId(userId).observe(getViewLifecycleOwner(), patientId -> {
                if (patientId != null && patientId > 0) {
                    appointmentViewModel.getAppointmentsByPatient(patientId).observe(getViewLifecycleOwner(), appointments -> {
                        this.appointmentList = appointments;
                        tryUpdateAdapter();
                    });
                } else {
                    this.appointmentList = new ArrayList<>();
                    tryUpdateAdapter();
                }
            });
        }
    }


    private void tryUpdateAdapter() {
        // El "guardián" que solo actualiza la UI cuando TODOS los datos están listos.
        if (appointmentList != null && patientList != null && doctorList != null) {
            adapter.setData(appointmentList, patientList, doctorList);
            binding.empty.setVisibility(appointmentList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.empty.setText("No hay citas registradas.");
        }
    }

    private void setupUI() {
        binding.fabAdd.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        if (isDoctor) {
            binding.fabAdd.setOnClickListener(v -> openForm(-1));
        }

        if (isDoctor) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    Appointment item = adapter.getAt(viewHolder.getAdapterPosition());
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Eliminar cita")
                            .setMessage("¿Desea eliminar esta cita?")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                appointmentViewModel.deleteById(item.getId());
                                Toast.makeText(getContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition()))
                            .show();
                }
            }).attachToRecyclerView(binding.recycler);
        }
    }

    private void openForm(int id) {
        Fragment f = AppointmentFormFragment.newInstance(id, !isDoctor);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, f)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(Appointment item) {
        openForm(item.getId());
    }
}
