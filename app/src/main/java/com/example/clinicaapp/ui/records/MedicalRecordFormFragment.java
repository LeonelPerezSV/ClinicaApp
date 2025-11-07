package com.example.clinicaapp.ui.records;

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

import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.databinding.FragmentMedicalRecordFormBinding;
import com.example.clinicaapp.viewmodel.MedicalRecordViewModel;
import com.example.clinicaapp.viewmodel.PatientViewModel;

public class MedicalRecordFormFragment extends Fragment {

    private FragmentMedicalRecordFormBinding binding;
    private MedicalRecordViewModel medicalRecordViewModel;
    private PatientViewModel patientViewModel;

    private int patientId;
    private MedicalRecord currentRecord;
    private boolean isDoctor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMedicalRecordFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            patientId = getArguments().getInt("patient_id", -1);
        }

        if (patientId == -1) {
            Toast.makeText(getContext(), "ID de paciente no válido", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        medicalRecordViewModel = new ViewModelProvider(this).get(MedicalRecordViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String userType = prefs.getString("user_type", "");
        isDoctor = "Doctor".equalsIgnoreCase(userType);

        setupUI();
        loadPatientInfo();
        loadMedicalRecord();
    }

    private void loadPatientInfo() {
        patientViewModel.getById(patientId).observe(getViewLifecycleOwner(), patient -> {
            if (patient != null) {
                binding.txtPatientName.setText("Expediente de: " + patient.getName());
            }
        });
    }

    private void loadMedicalRecord() {
        medicalRecordViewModel.getRecordByPatientId(patientId).observe(getViewLifecycleOwner(), record -> {
            if (record != null) {
                currentRecord = record;
            } else {
                currentRecord = new MedicalRecord();
                currentRecord.setPatientId(patientId);
                currentRecord.setSummary("Sin diagnóstico inicial");
                currentRecord.setAllergies("Sin alergias registradas");
                currentRecord.setNotes("");
            }
            binding.inputSummary.setText(currentRecord.getSummary());
            binding.inputAllergies.setText(currentRecord.getAllergies());
            binding.inputNotes.setText(currentRecord.getNotes());
        });
    }

    private void setupUI() {
        if (isDoctor) {
            binding.btnSave.setOnClickListener(v -> saveMedicalRecord());
        } else {
            binding.inputSummary.setEnabled(false);
            binding.inputAllergies.setEnabled(false);
            binding.inputNotes.setEnabled(false);
            binding.btnSave.setVisibility(View.GONE);
        }
        binding.btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    //Ahora distingue entre crear un nuevo expediente y actualizar uno existente.
    private void saveMedicalRecord() {
        if (currentRecord == null) {
            Toast.makeText(getContext(), "Error: no se pudo cargar el expediente", Toast.LENGTH_SHORT).show();
            return;
        }

        currentRecord.setSummary(binding.inputSummary.getText().toString().trim());
        currentRecord.setAllergies(binding.inputAllergies.getText().toString().trim());
        currentRecord.setNotes(binding.inputNotes.getText().toString().trim());

        //Comprobar si el ID es 0 (o nulo) para decidir si insertar o actualizar.
        //El ID de un objeto nuevo que aún no ha sido insertado es 0 por defecto.
        if (currentRecord.getId() == 0) {
            medicalRecordViewModel.insert(currentRecord);
            Toast.makeText(getContext(), "Expediente creado con éxito", Toast.LENGTH_SHORT).show();
        } else {
            medicalRecordViewModel.update(currentRecord);
            Toast.makeText(getContext(), "Expediente actualizado con éxito", Toast.LENGTH_SHORT).show();
        }

        getParentFragmentManager().popBackStack();
    }
}
