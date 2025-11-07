package com.example.clinicaapp.ui.prescriptions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.databinding.FragmentPrescriptionFormBinding;
import com.example.clinicaapp.viewmodel.PatientViewModel;
import com.example.clinicaapp.viewmodel.PrescriptionViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PrescriptionFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";
    private static final String ARG_READ_ONLY = "readOnly";

    private FragmentPrescriptionFormBinding binding;
    private PrescriptionViewModel prescriptionViewModel;
    private PatientViewModel patientViewModel;

    private Integer currentId = null;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    exportPdf();
                } else {
                    Toast.makeText(getContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public static PrescriptionFormFragment newInstance(int id, boolean readOnly) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putBoolean(ARG_READ_ONLY, readOnly);
        PrescriptionFormFragment fragment = new PrescriptionFormFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrescriptionFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prescriptionViewModel = new ViewModelProvider(this).get(PrescriptionViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        setupUI();
        setupPatientObserver();

        if (getArguments() != null && getArguments().containsKey(ARG_ID)) {
            int id = getArguments().getInt(ARG_ID, -1);
            if (id != -1) {
                currentId = id;
                loadExistingPrescription(id);
            }
        }
    }

    private void setupPatientObserver() {
        patientViewModel.getAll().observe(getViewLifecycleOwner(), patients -> {
            if (patients == null) return;
            List<String> patientNames = patients.stream()
                    .map(p -> p.getId() + " - " + p.getName())
                    .collect(Collectors.toList());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, patientNames);
            binding.spinnerPatient.setAdapter(adapter);

            if (currentId != null) loadExistingPrescription(currentId);
        });
    }

    private void setupUI() {
        binding.inputDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection));
                binding.inputDate.setText(d);
            });
            picker.show(getParentFragmentManager(), "datePicker");
        });

        binding.btnSave.setOnClickListener(v -> savePrescription());
        binding.btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnExportPdf.setOnClickListener(v -> checkPermissionAndExport());

        if (getArguments() != null && getArguments().getBoolean(ARG_READ_ONLY, false)) {
            binding.spinnerPatient.setEnabled(false);
            binding.inputDate.setEnabled(false);
            binding.inputMedication.setEnabled(false);
            binding.inputDosage.setEnabled(false);
            binding.inputNotes.setEnabled(false);
            binding.btnSave.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
            binding.btnExportPdf.setVisibility(View.VISIBLE); // Asegurarse de que el paciente pueda exportar
        }
    }

    private void loadExistingPrescription(int id) {
        prescriptionViewModel.getById(id).observe(getViewLifecycleOwner(), p -> {
            if (p == null) return;
            currentId = p.getId();
            binding.inputDate.setText(p.getDate());
            binding.inputMedication.setText(p.getMedication());
            binding.inputDosage.setText(p.getDosage());
            binding.inputNotes.setText(p.getNotes());

            selectDropdownValue(binding.spinnerPatient, p.getPatientId());

            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(v -> {
                prescriptionViewModel.delete(p);
                Toast.makeText(getContext(), "Receta eliminada", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        });
    }

    private void selectDropdownValue(AutoCompleteTextView dropdown, int idToSelect) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) dropdown.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null && item.startsWith(idToSelect + " - ")) {
                dropdown.setText(item, false);
                break;
            }
        }
    }

    private void savePrescription() {
        String patientString = binding.spinnerPatient.getText().toString();
        String date = binding.inputDate.getText().toString();
        String medication = binding.inputMedication.getText().toString();
        String dosage = binding.inputDosage.getText().toString();

        if (patientString.isEmpty() || date.isEmpty() || medication.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = Integer.parseInt(patientString.split(" - ")[0]);

        Prescription prescription = new Prescription();
        if (currentId != null) {
            prescription.setId(currentId);
        }
        prescription.setPatientId(patientId);
        prescription.setDate(date);
        prescription.setMedication(medication);
        prescription.setDosage(dosage);
        prescription.setNotes(binding.inputNotes.getText().toString());

        if (currentId == null) {
            prescriptionViewModel.insert(prescription);
            Toast.makeText(getContext(), "Receta creada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            prescriptionViewModel.update(prescription);
            Toast.makeText(getContext(), "Receta actualizada", Toast.LENGTH_SHORT).show();
        }

        getParentFragmentManager().popBackStack();
    }

    private void checkPermissionAndExport() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportPdf();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void exportPdf() {
        String patientString = binding.spinnerPatient.getText().toString();
        String date = binding.inputDate.getText().toString();
        String medication = binding.inputMedication.getText().toString();
        String dosage = binding.inputDosage.getText().toString();
        String notes = binding.inputNotes.getText().toString();

        if (patientString.isEmpty()) {
            Toast.makeText(getContext(), "No se puede exportar sin un paciente seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }

        String patientName = patientString.split(" - ")[1];

        try {
            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = doc.startPage(info);
            Canvas canvas = page.getCanvas();

            Paint title = new Paint();
            title.setTextSize(20f);
            title.setFakeBoldText(true);

            Paint text = new Paint();
            text.setTextSize(14f);

            int x = 40, y = 60, dy = 28;

            canvas.drawText("CLINICAPP – Receta Médica", x, y, title);
            y += dy;
            canvas.drawLine(x, y, info.getPageWidth() - x, y, text);
            y += dy;

            canvas.drawText("Fecha: " + date, x, y, text);
            y += dy;
            canvas.drawText("Paciente: " + patientName, x, y, text);
            y += dy;
            y += dy; // Espacio extra

            canvas.drawText("Medicamento:", x, y, title);
            y += dy;
            canvas.drawText(medication, x, y, text);
            y += dy;

            canvas.drawText("Dosis:", x, y, title);
            y += dy;
            canvas.drawText(dosage, x, y, text);
            y += dy;

            if (!TextUtils.isEmpty(notes)) {
                canvas.drawText("Notas Adicionales:", x, y, title);
                y += dy;
                canvas.drawText(notes, x, y, text);
            }

            doc.finishPage(page);

            File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(outDir, "receta_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                doc.writeTo(fos);
            }
            doc.close();

            openPdfFile(pdfFile);

        } catch (IOException e) {
            Log.e("PdfExport", "Error al generar PDF de receta", e);
            Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir con"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se encontró visor PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
