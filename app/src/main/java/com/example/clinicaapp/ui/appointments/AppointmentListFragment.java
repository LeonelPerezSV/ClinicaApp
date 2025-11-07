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

import java.util.List;

public class AppointmentListFragment extends Fragment implements AppointmentAdapter.OnAppointmentClick {

    private FragmentAppointmentListBinding binding;
    private AppointmentViewModel appointmentViewModel;
    private PatientViewModel patientViewModel;
    private DoctorViewModel doctorViewModel;
    private AppointmentAdapter adapter;

    private boolean isDoctor;
    private long userId;

    // Listas para almacenar los datos de los observadores
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
        userId = prefs.getLong("user_id", 0);
        isDoctor = "Doctor".equalsIgnoreCase(type);

        // Inicializar todos los ViewModels
        appointmentViewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        // Configurar el RecyclerView y el Adaptador
        adapter = new AppointmentAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        // Configurar los observadores que trabajarán juntos
        setupObservers();

        // configurar el resto de la UI
        setupUI();
    }

     //Configura los observadores para las 3 listas de datos.
     //El adaptador solo se actualiza cuando se han recibido los tres conjuntos de datos.

    private void setupObservers() {
        patientViewModel.getAll().observe(getViewLifecycleOwner(), patients -> {
            this.patientList = patients;
            tryUpdateAdapter();
        });

        doctorViewModel.getAll().observe(getViewLifecycleOwner(), doctors -> {
            this.doctorList = doctors;
            tryUpdateAdapter();
        });

        // El observador de citas determina QUÉ citas mostrar (todas o solo las del paciente)
        if (isDoctor) {
            appointmentViewModel.getAllAppointments().observe(getViewLifecycleOwner(), appointments -> {
                this.appointmentList = appointments;
                tryUpdateAdapter();
            });
        } else {
            // La lógica para obtener las citas de un paciente específico se puede mejorar en el futuro,
            // pero por ahora la mantenemos para no romper la funcionalidad existente en AppointmentViewModel.
            patientViewModel.getPatientIdByUserId((int) userId).observe(getViewLifecycleOwner(), patientId -> {
                if (patientId != null && patientId > 0) {
                    appointmentViewModel.getAppointmentsByPatient(patientId).observe(getViewLifecycleOwner(), appointments -> {
                        this.appointmentList = appointments;
                        tryUpdateAdapter();
                    });
                }
            });
        }
    }

    //Intenta actualizar el adaptador. Solo lo hace si las 3 listas de datos han sido recibidas.

    private void tryUpdateAdapter() {
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

        // Configurar el gesto de deslizar para eliminar (solo para doctores)
        if (isDoctor) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
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
