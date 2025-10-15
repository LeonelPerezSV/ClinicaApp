package com.example.clinicaapp.ui.prescriptions;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.databinding.FragmentPrescriptionFormBinding;
import com.example.clinicaapp.viewmodel.PrescriptionViewModel;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrescriptionFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";
    public static PrescriptionFormFragment newInstance(int id) {
        Bundle b = new Bundle(); b.putInt(ARG_ID, id);
        PrescriptionFormFragment f = new PrescriptionFormFragment(); f.setArguments(b); return f;
    }

    private FragmentPrescriptionFormBinding binding;
    private PrescriptionViewModel viewModel;
    private Integer currentId = null;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrescriptionFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PrescriptionViewModel.class);

        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            currentId = id;
            viewModel.getById(id).observe(getViewLifecycleOwner(), p -> {
                if (p != null) {
                    binding.inputPatientId.setText(String.valueOf(p.getPatientId()));
                    binding.inputDoctorId.setText(String.valueOf(p.getDoctorId()));
                    binding.inputDate.setText(p.getDate()); // asumo String
                    binding.inputMedication.setText(p.getMedication());
                    binding.inputDosage.setText(p.getDosage());
                    binding.inputNotes.setText(p.getNotes());
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> {
            String patientIdStr = binding.inputPatientId.getText().toString().trim();
            String doctorIdStr  = binding.inputDoctorId.getText().toString().trim();
            String medication   = binding.inputMedication.getText().toString().trim();

            if (TextUtils.isEmpty(patientIdStr) || TextUtils.isEmpty(doctorIdStr) || TextUtils.isEmpty(medication)) {
                Toast.makeText(getContext(), "Paciente, Doctor y Medicación son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            Prescription p = new Prescription();
            if (currentId != null) p.setId(currentId);
            p.setPatientId(Integer.parseInt(patientIdStr));
            p.setDoctorId(Integer.parseInt(doctorIdStr));
            p.setDate(binding.inputDate.getText().toString().trim());
            p.setMedication(medication);
            p.setDosage(binding.inputDosage.getText().toString().trim());
            p.setNotes(binding.inputNotes.getText().toString().trim());

            if (currentId == null) {
                viewModel.insert(p);
                Toast.makeText(getContext(), "Receta creada", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.update(p);
                Toast.makeText(getContext(), "Receta actualizada", Toast.LENGTH_SHORT).show();
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }
}
