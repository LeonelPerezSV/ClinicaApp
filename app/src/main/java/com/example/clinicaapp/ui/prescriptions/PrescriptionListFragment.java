package com.example.clinicaapp.ui.prescriptions;

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
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.databinding.FragmentPrescriptionListBinding;
import com.example.clinicaapp.viewmodel.PrescriptionViewModel;

import java.util.List;
import java.util.concurrent.Executors;

public class PrescriptionListFragment extends Fragment implements PrescriptionAdapter.OnPrescriptionClick {

    private FragmentPrescriptionListBinding binding;
    private PrescriptionViewModel viewModel;
    private PrescriptionAdapter adapter;
    private boolean isDoctor;
    private long userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrescriptionListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String type = prefs.getString("user_type", "Paciente");
        userId = prefs.getLong("user_id", 0);
        isDoctor = "Doctor".equalsIgnoreCase(type);

        viewModel = new ViewModelProvider(this).get(PrescriptionViewModel.class);
        adapter = new PrescriptionAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        if (isDoctor) {
            // 👨‍⚕️ Doctor → todas las recetas
            viewModel.getAllPrescriptions().observe(getViewLifecycleOwner(), this::updateList);
        } else {
            // 👤 Paciente → buscar su patientId real y mostrar solo sus recetas
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    int patientId = db.patientDao().getPatientIdByUserId((int) userId);

                    android.util.Log.d("CLINICAPP", "userId=" + userId + ", patientId=" + patientId);

                    requireActivity().runOnUiThread(() -> {
                        if (patientId > 0) {
                            viewModel.getPrescriptionsByPatient(patientId)
                                    .observe(getViewLifecycleOwner(), list -> {
                                        if (list != null && !list.isEmpty()) {
                                            adapter.submit(list);
                                            binding.empty.setVisibility(View.GONE);
                                        } else {
                                            binding.empty.setVisibility(View.VISIBLE);
                                            binding.empty.setText("No tienes recetas registradas.");
                                        }
                                    });
                        } else {
                            binding.empty.setVisibility(View.VISIBLE);
                            binding.empty.setText("No se encontró un paciente vinculado a este usuario.");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Error al cargar recetas: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                }
            });
        }



        // FAB solo visible para doctor
        binding.fabAdd.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        if (isDoctor) {
            binding.fabAdd.setOnClickListener(v -> openForm(-1));
        }

        // Swipe para eliminar recetas solo si es doctor
        if (isDoctor) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder v1, @NonNull RecyclerView.ViewHolder v2) { return false; }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    Prescription item = adapter.getAt(vh.getAdapterPosition());
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Desea eliminar la receta del paciente #" + item.getPatientId() + "?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                viewModel.deleteById(item.getId());
                                Toast.makeText(getContext(), "Receta eliminada correctamente", Toast.LENGTH_SHORT).show();
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

    private void updateList(List<Prescription> list) {
        adapter.submit(list);
        binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openForm(int id) {
        // 🧠 Si el usuario es paciente, abrimos el formulario en modo solo lectura
        Fragment f = PrescriptionFormFragment.newInstance(id, !isDoctor);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, f)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(Prescription item) {
        openForm(item.getId());
    }
}
