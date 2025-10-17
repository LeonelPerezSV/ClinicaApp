package com.example.clinicaapp.ui.prescriptions;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.databinding.FragmentPrescriptionFormBinding;
import com.example.clinicaapp.ui.appointments.AppointmentFormFragment;
import com.google.android.material.datepicker.MaterialDatePicker;

// 👇 FALTABA ESTE IMPORT
import com.example.clinicaapp.viewmodel.PrescriptionViewModel;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.content.Intent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.Locale;
import java.util.stream.Collectors;

public class PrescriptionFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";

    public static PrescriptionFormFragment newInstance(int id) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, id);
        PrescriptionFormFragment f = new PrescriptionFormFragment();
        f.setArguments(b);
        return f;
    }

    // Permite abrir en modo solo lectura (paciente)
    // ✅ NUEVO: versión sobrecargada para abrir en modo solo lectura (paciente)

    public static PrescriptionFormFragment newInstance(int id, boolean readOnly) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, id);
        b.putBoolean("readOnly", readOnly);
        PrescriptionFormFragment f = new PrescriptionFormFragment();
        f.setArguments(b);
        return f;
    }


    private FragmentPrescriptionFormBinding binding;
    private PrescriptionViewModel viewModel;
    private Integer currentId = null;
    private Integer preselectedPatientId = null;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrescriptionFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PrescriptionViewModel.class);

        // Fecha por defecto
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        binding.inputDate.setText(today);

        // DatePicker
        binding.inputDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccionar fecha de la receta")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                String formatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(new Date(selection));
                binding.inputDate.setText(formatted);
            });
            picker.show(getParentFragmentManager(), "datePickerPrescription");
        });

        // Cargar pacientes para el spinner
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Patient> patients = db.patientDao().getAllPatientsList();

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> pAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        patients.stream()
                                .map(p -> p.getId() + " - " + (p.getFirstName() + " " + p.getLastName()).trim())
                                .collect(Collectors.toList())
                );
                pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerPatient.setAdapter(pAdapter);

                if (getArguments() != null && getArguments().containsKey(ARG_ID)) {
                    cargarRecetaExistente(getArguments().getInt(ARG_ID));
                }
            });
        });

        boolean readOnly = getArguments() != null && getArguments().getBoolean("readOnly", false);
        if (readOnly) {
            binding.btnSave.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
            binding.btnExportPdf.setVisibility(View.GONE);
            binding.spinnerPatient.setEnabled(false);
            binding.inputDate.setEnabled(false);
            binding.inputMedication.setEnabled(false);
            binding.inputDosage.setEnabled(false);
            binding.inputNotes.setEnabled(false);
        }

        binding.btnSave.setOnClickListener(v -> guardarReceta());
        binding.btnExportPdf.setOnClickListener(v -> exportPdf());
        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void cargarRecetaExistente(int id) {
        viewModel.getById(id).observe(getViewLifecycleOwner(), r -> {
            if (r != null) {
                isEditMode = true;
                currentId = id;
                preselectedPatientId = r.getPatientId();

                binding.inputDate.setText(r.getDate());
                binding.inputMedication.setText(r.getMedication());
                binding.inputDosage.setText(r.getDosage());
                binding.inputNotes.setText(r.getNotes());

                trySelectPatientInSpinner();

                binding.btnDelete.setVisibility(View.VISIBLE);
                binding.btnDelete.setOnClickListener(v -> {
                    viewModel.deleteById(r.getId());
                    Toast.makeText(getContext(), "Receta eliminada", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }
        });
    }

    private void trySelectPatientInSpinner() {
        if (!isEditMode || preselectedPatientId == null || binding.spinnerPatient.getAdapter() == null) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) binding.spinnerPatient.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = (String) adapter.getItem(i);
            if (item.startsWith(preselectedPatientId + " -")) {
                binding.spinnerPatient.setSelection(i);
                binding.spinnerPatient.setEnabled(false);
                break;
            }
        }
    }

    private void guardarReceta() {
        String selectedPatient = (String) binding.spinnerPatient.getSelectedItem();
        String date = binding.inputDate.getText().toString().trim();
        String medication = binding.inputMedication.getText().toString().trim();
        String dosage = binding.inputDosage.getText().toString().trim();
        String notes = binding.inputNotes.getText().toString().trim();

        if (selectedPatient == null || TextUtils.isEmpty(date) || TextUtils.isEmpty(medication)) {
            Toast.makeText(getContext(), "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = Integer.parseInt(selectedPatient.split(" - ")[0]);
        Prescription p = new Prescription();
        if (currentId != null) p.setId(currentId);

        p.setPatientId(patientId);
        p.setDate(date);
        p.setMedication(medication);
        p.setDosage(dosage);
        p.setNotes(notes);

        if (currentId == null) {
            viewModel.insert(p);
            Toast.makeText(getContext(), "Receta creada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(p);
            Toast.makeText(getContext(), "Receta actualizada", Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void exportPdf() {
        String selectedPatient = (String) binding.spinnerPatient.getSelectedItem();
        if (selectedPatient == null) {
            Toast.makeText(getContext(), "Seleccione un paciente", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = binding.inputDate.getText().toString().trim();
        String medication = binding.inputMedication.getText().toString().trim();
        String dosage = binding.inputDosage.getText().toString().trim();
        String notes = binding.inputNotes.getText().toString().trim();

        int patientId = Integer.parseInt(selectedPatient.split(" - ")[0]);
        String patientName = selectedPatient.split(" - ")[1];

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
            canvas.drawText("Paciente: " + patientId + " - " + patientName, x, y, text);
            y += dy;
            canvas.drawText("Medicamento: " + (TextUtils.isEmpty(medication) ? "—" : medication), x, y, text);
            y += dy;
            canvas.drawText("Dosis/Frecuencia: " + (TextUtils.isEmpty(dosage) ? "—" : dosage), x, y, text);
            y += dy;
            canvas.drawText("Notas:", x, y, title);
            y += dy;
            canvas.drawText(TextUtils.isEmpty(notes) ? "—" : notes, x, y, text);

            doc.finishPage(page);

            File outDir = requireContext().getExternalFilesDir(null);
            File pdfFile = new File(outDir, "receta_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                doc.writeTo(fos);
            }
            doc.close();

            openPdfFile(pdfFile);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir con"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se encontró visor PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
