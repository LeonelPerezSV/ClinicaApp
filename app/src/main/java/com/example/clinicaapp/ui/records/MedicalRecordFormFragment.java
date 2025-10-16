package com.example.clinicaapp.ui.records;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.databinding.FragmentMedicalRecordFormBinding;
import com.example.clinicaapp.viewmodel.MedicalRecordViewModel;

public class MedicalRecordFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";
    public static MedicalRecordFormFragment newInstance(int id) {
        Bundle b = new Bundle(); b.putInt(ARG_ID, id);
        MedicalRecordFormFragment f = new MedicalRecordFormFragment(); f.setArguments(b); return f;
    }

    private FragmentMedicalRecordFormBinding binding;
    private MedicalRecordViewModel viewModel;
    private Integer currentId = null;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMedicalRecordFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MedicalRecordViewModel.class);

        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            currentId = id;
            viewModel.getById(id).observe(getViewLifecycleOwner(), r -> {
                if (r != null) {
                    binding.inputPatientId.setText(String.valueOf(r.getPatientId()));
                    binding.inputSummary.setText(r.getSummary());
                    binding.inputAllergies.setText(r.getAllergies());
                    binding.inputNotes.setText(r.getNotes());

                    // 🔍 Mostrar nombre del paciente
                    new Thread(() -> {
                        try {
                            com.example.clinicaapp.data.entities.Patient p =
                                    com.example.clinicaapp.data.db.AppDatabase
                                            .getInstance(requireContext())
                                            .patientDao()
                                            .findById(r.getPatientId());
                            if (p != null && getActivity() != null) {
                                requireActivity().runOnUiThread(() -> {
                                    binding.txtPatientName.setText("Paciente: " + p.getName() + " (ID " + p.getId() + ")");
                                });
                            }
                        } catch (Exception ignored) {}
                    }).start();
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> {
            String pStr = binding.inputPatientId.getText().toString().trim();
            String summary = binding.inputSummary.getText().toString().trim();

            if (TextUtils.isEmpty(pStr) || TextUtils.isEmpty(summary)) {
                Toast.makeText(getContext(), "Paciente y Resumen son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            MedicalRecord r = new MedicalRecord();
            if (currentId != null) r.setId(currentId);
            r.setPatientId(Integer.parseInt(pStr));
            r.setSummary(summary);
            r.setAllergies(binding.inputAllergies.getText().toString().trim());
            r.setNotes(binding.inputNotes.getText().toString().trim());

            if (currentId == null) {
                viewModel.insert(r);
                Toast.makeText(getContext(), "Historial creado", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.update(r);
                Toast.makeText(getContext(), "Historial actualizado", Toast.LENGTH_SHORT).show();
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }
}
