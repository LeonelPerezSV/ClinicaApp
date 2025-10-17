package com.example.clinicaapp.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.List;
import java.util.concurrent.Executors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
            // 👨‍⚕️ El doctor ve todas las citas
            viewModel.getAllAppointments().observe(getViewLifecycleOwner(), this::updateList);
        } else {
            // 👤 El paciente solo ve sus citas
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    int patientId = AppDatabase.getInstance(requireContext())
                            .patientDao()
                            .getPatientIdByUserId((int) userId);

                    requireActivity().runOnUiThread(() -> {
                        if (patientId > 0) {
                            viewModel.getAppointmentsByPatient(patientId)
                                    .observe(getViewLifecycleOwner(), this::updateList);
                        } else {
                            binding.empty.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(), "No se encontró el paciente vinculado.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error al cargar citas: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            });
        }

        // FAB solo visible para doctor
        binding.fabAdd.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        if (isDoctor) {
            binding.fabAdd.setOnClickListener(v -> openForm(-1));
        }

        // Swipe para eliminar citas solo si es doctor
        if (isDoctor) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder v1, @NonNull RecyclerView.ViewHolder v2) { return false; }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    Appointment item = adapter.getAt(vh.getAdapterPosition());
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Desea eliminar la cita del paciente #" + item.getPatientId() + "?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                viewModel.deleteById(item.getId());
                                Toast.makeText(getContext(), "Cita eliminada correctamente", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                adapter.notifyItemChanged(vh.getAdapterPosition());
                                dialog.dismiss();
                            })
                            .show();
                }
            }).attachToRecyclerView(binding.recycler);
        }
    }

    private void updateList(List<Appointment> list) {
        adapter.submit(list);
        binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
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
