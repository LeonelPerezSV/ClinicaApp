package com.example.clinicaapp.ui.prescriptions;

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
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.databinding.FragmentPrescriptionListBinding;
import com.example.clinicaapp.viewmodel.PatientViewModel;
import com.example.clinicaapp.viewmodel.PrescriptionViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class PrescriptionListFragment extends Fragment implements PrescriptionAdapter.OnPrescriptionClick {

    private FragmentPrescriptionListBinding binding;
    private PrescriptionViewModel prescriptionViewModel;
    private PatientViewModel patientViewModel; // [NUEVO] Para obtener el ID del paciente
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

        // Inicializar los ViewModels
        prescriptionViewModel = new ViewModelProvider(this).get(PrescriptionViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // Configurar el RecyclerView
        adapter = new PrescriptionAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        // Configurar los observadores de datos
        setupObservers();

        // Configurar la UI (FAB y gesto de swipe)
        setupUI();
    }

    //Configura la lógica para obtener datos a través de los ViewModels.

    private void setupObservers() {
        if (isDoctor) {
            // 👨‍⚕️ Si es Doctor, muestra todas las recetas
            prescriptionViewModel.getAllPrescriptions().observe(getViewLifecycleOwner(), this::updateList);
        } else {
            // 👤 Si es Paciente, busca su ID de paciente y luego sus recetas
            patientViewModel.getPatientIdByUserId((int) userId).observe(getViewLifecycleOwner(), patientId -> {
                if (patientId != null && patientId > 0) {
                    // Una vez que tenemos el patientId, observamos sus recetas
                    prescriptionViewModel.getPrescriptionsByPatient(patientId).observe(getViewLifecycleOwner(), this::updateList);
                } else {
                    // No se encontró un paciente para este usuario
                    binding.empty.setVisibility(View.VISIBLE);
                    binding.empty.setText("No se encontró un paciente vinculado a este usuario.");
                }
            });
        }
    }

    private void setupUI() {
        // FAB solo visible para doctor
        binding.fabAdd.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        if (isDoctor) {
            binding.fabAdd.setOnClickListener(v -> openForm(-1));
        }

        // Swipe para eliminar solo si es doctor
        if (isDoctor) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh1, @NonNull RecyclerView.ViewHolder vh2) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    Prescription item = adapter.getAt(vh.getAdapterPosition());
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Desea eliminar esta receta?")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                prescriptionViewModel.deleteById(item.getId());
                                Toast.makeText(getContext(), "Receta eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> adapter.notifyItemChanged(vh.getAdapterPosition()))
                            .show();
                }
            }).attachToRecyclerView(binding.recycler);
        }
    }

    private void updateList(List<Prescription> list) {
        adapter.submit(list);
        binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        if (list == null || list.isEmpty()) {
            binding.empty.setText("No hay recetas registradas.");
        }
    }

    private void openForm(int id) {
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
