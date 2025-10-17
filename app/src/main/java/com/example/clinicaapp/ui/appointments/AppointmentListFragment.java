package com.example.clinicaapp.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.databinding.FragmentAppointmentListBinding;
import com.example.clinicaapp.viewmodel.AppointmentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.Executors;

public class AppointmentListFragment extends Fragment implements AppointmentAdapter.OnAppointmentClick {

    private FragmentAppointmentListBinding binding;
    private AppointmentViewModel viewModel;
    private AppointmentAdapter adapter;
    private boolean isDoctor;
    private long userId;

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

        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        adapter = new AppointmentAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        if (isDoctor) {
            // 👨‍⚕️ Doctor → todas las citas
            viewModel.getAllAppointments().observe(getViewLifecycleOwner(), this::updateList);
        } else {
            // 👤 Paciente → buscar su patientId y cargar solo sus citas
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    int patientId = db.patientDao().getPatientIdByUserId((int) userId);

                    Log.d("CLINICAPP", "userId=" + userId + ", patientId=" + patientId);

                    requireActivity().runOnUiThread(() -> {
                        if (patientId > 0) {
                            // 🔹 Ahora sí, observar citas de ese paciente
                            viewModel.getAppointmentsByPatient(patientId)
                                    .observe(getViewLifecycleOwner(), list -> {
                                        if (list != null && !list.isEmpty()) {
                                            adapter.submit(list);
                                            binding.empty.setVisibility(View.GONE);
                                        } else {
                                            binding.empty.setVisibility(View.VISIBLE);
                                            binding.empty.setText("No tienes citas registradas.");
                                        }
                                    });
                        } else {
                            binding.empty.setVisibility(View.VISIBLE);
                            binding.empty.setText("No se encontró el paciente vinculado a este usuario.");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Error al cargar citas: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                }
            });
        }

        // 🔹 FAB visible solo para doctor
        binding.fabAdd.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        if (isDoctor) {
            binding.fabAdd.setOnClickListener(v -> openForm(-1));
        }

// 🔹 Configurar Swipe (solo doctor puede eliminar)
        if (isDoctor) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    Appointment item = adapter.getAt(viewHolder.getAdapterPosition());

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Eliminar cita")
                            .setMessage("¿Desea eliminar la cita del paciente #" + item.getPatientId() + "?")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                viewModel.deleteById(item.getId());
                                Toast.makeText(getContext(), "Cita eliminada correctamente", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                dialog.dismiss();
                            })
                            .show();
                }
            }).attachToRecyclerView(binding.recycler);
        } else {
            // 🔹 Si es paciente, bloquear Swipe
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    // No hace nada
                }
            }).attachToRecyclerView(binding.recycler);
        }

    }

    private void updateList(List<Appointment> list) {
        adapter.submit(list);
        if (list == null || list.isEmpty()) {
            binding.empty.setVisibility(View.VISIBLE);
            binding.empty.setText("No hay citas registradas.");
        } else {
            binding.empty.setVisibility(View.GONE);
        }
    }

    private void openForm(int id) {
        // 🔹 Si es paciente → abrir en modo solo lectura
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
