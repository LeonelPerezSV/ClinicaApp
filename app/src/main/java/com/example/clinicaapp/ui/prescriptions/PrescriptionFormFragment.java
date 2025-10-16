package com.example.clinicaapp.ui.prescriptions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.repo.DoctorRepository;
import com.example.clinicaapp.databinding.FragmentPrescriptionFormBinding;
import com.example.clinicaapp.viewmodel.PrescriptionViewModel;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
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

    private FragmentPrescriptionFormBinding binding;
    private PrescriptionViewModel viewModel;
    private Integer currentId = null;
    private Doctor loggedDoctor = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrescriptionFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PrescriptionViewModel.class);

        // =====================================================
        // 👨‍⚕️ Detectar si el usuario logueado es un Doctor
        // =====================================================
        SharedPreferences prefs = requireContext().getSharedPreferences("ClinicaAppPrefs", requireContext().MODE_PRIVATE);
        String userType = prefs.getString("user_type", "");
        String userName = prefs.getString("user_name", "");

        if ("Doctor".equalsIgnoreCase(userType)) {
            try {
                DoctorRepository repo = new DoctorRepository(requireContext());
                // Obtener lista sincrónica de doctores
                List<Doctor> all = repo.getAllList();

                if (all != null && !all.isEmpty()) {
                    for (Doctor d : all) {
                        // Coincidencia flexible: sin espacios extra, ignorando mayúsculas
                        String doctorName = d.getName().trim().toLowerCase();
                        String sessionName = userName.trim().toLowerCase();
                        if (doctorName.contains(sessionName) || sessionName.contains(doctorName)) {
                            loggedDoctor = d;
                            break;
                        }
                    }
                }

                if (loggedDoctor != null) {
                    // 🔹 Ocultar spinner y mostrar nombre del doctor autenticado
                    binding.spinnerDoctor.setVisibility(View.GONE);

                    TextView fixedDoctor = new TextView(requireContext());
                    fixedDoctor.setText("👨‍⚕️ Doctor: " + loggedDoctor.getName());
                    fixedDoctor.setTextSize(16f);
                    fixedDoctor.setPadding(0, 16, 0, 16);
                    ((ViewGroup) binding.spinnerDoctor.getParent()).addView(fixedDoctor, 2);
                } else {
                    Toast.makeText(getContext(), "No se encontró el perfil del doctor en la base de datos", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al cargar el doctor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


        // =====================================================
        // 🔽 Cargar pacientes y doctores en los Spinners
        // =====================================================
        viewModel.getAllPatients().observe(getViewLifecycleOwner(), patients -> {
            if (patients != null && !patients.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        patients.stream()
                                .map(p -> p.getId() + " - " + p.getName())
                                .collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerPatient.setAdapter(adapter);
            }
        });

        viewModel.getAllDoctors().observe(getViewLifecycleOwner(), doctors -> {
            if (doctors != null && !doctors.isEmpty() && loggedDoctor == null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        doctors.stream()
                                .map(d -> d.getId() + " - " + d.getName())
                                .collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerDoctor.setAdapter(adapter);
            }
        });

        // =====================================================
        // 📝 Cargar datos si se edita receta existente
        // =====================================================
        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            currentId = id;
            binding.btnDelete.setVisibility(View.VISIBLE);

            viewModel.getById(id).observe(getViewLifecycleOwner(), p -> {
                if (p != null) {
                    binding.inputDate.setText(p.getDate());
                    binding.inputMedication.setText(p.getMedication());
                    binding.inputDosage.setText(p.getDosage());
                    binding.inputNotes.setText(p.getNotes());
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                viewModel.deleteById(currentId);
                Toast.makeText(getContext(), "Receta eliminada", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        binding.btnSave.setOnClickListener(v -> savePrescription());
        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnExportPdf.setOnClickListener(v -> exportPdf());
    }

    // =====================================================
    // 💾 Guardar receta (auto doctor si aplica)
    // =====================================================
    private void savePrescription() {
        String selectedPatient = (String) binding.spinnerPatient.getSelectedItem();
        if (selectedPatient == null || TextUtils.isEmpty(binding.inputMedication.getText().toString().trim())) {
            Toast.makeText(getContext(), "Seleccione Paciente y complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = Integer.parseInt(selectedPatient.split(" - ")[0]);
        int doctorId;
        String doctorName;

        if (loggedDoctor != null) {
            doctorId = loggedDoctor.getId();
            doctorName = loggedDoctor.getName();
        } else {
            String selectedDoctor = (String) binding.spinnerDoctor.getSelectedItem();
            if (selectedDoctor == null) {
                Toast.makeText(getContext(), "Seleccione un doctor", Toast.LENGTH_SHORT).show();
                return;
            }
            doctorId = Integer.parseInt(selectedDoctor.split(" - ")[0]);
            doctorName = selectedDoctor.split(" - ")[1];
        }

        Prescription p = new Prescription();
        if (currentId != null) p.setId(currentId);
        p.setPatientId(patientId);
        p.setDoctorId(doctorId);
        p.setDate(binding.inputDate.getText().toString().trim());
        p.setMedication(binding.inputMedication.getText().toString().trim());
        p.setDosage(binding.inputDosage.getText().toString().trim());
        p.setNotes(binding.inputNotes.getText().toString().trim());

        if (currentId == null) {
            viewModel.insert(p);
            Toast.makeText(getContext(), "Receta creada por el Dr. " + doctorName, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(p);
            Toast.makeText(getContext(), "Receta actualizada", Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    // =====================================================
    // 🧾 Generación del PDF de la receta
    // =====================================================
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

        int doctorId;
        String doctorName;

        if (loggedDoctor != null) {
            doctorId = loggedDoctor.getId();
            doctorName = loggedDoctor.getName();
        } else {
            String selectedDoctor = (String) binding.spinnerDoctor.getSelectedItem();
            doctorId = Integer.parseInt(selectedDoctor.split(" - ")[0]);
            doctorName = selectedDoctor.split(" - ")[1];
        }

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

            canvas.drawText("Fecha: " + (TextUtils.isEmpty(date) ? "—" : date), x, y, text);
            y += dy;
            canvas.drawText("Paciente: " + patientId + " - " + patientName, x, y, text);
            y += dy;
            canvas.drawText("Doctor: " + doctorId + " - " + doctorName, x, y, text);
            y += dy;

            y += 10;
            canvas.drawText("Medicamento:", x, y, title);
            y += dy;
            canvas.drawText(medication, x, y, text);
            y += dy;

            canvas.drawText("Dosis:", x, y, title);
            y += dy;
            canvas.drawText(TextUtils.isEmpty(dosage) ? "—" : dosage, x, y, text);
            y += dy;

            canvas.drawText("Notas:", x, y, title);
            y += dy;
            String[] lines = wrap(notes, 60);
            for (String line : lines) {
                canvas.drawText(line, x, y, text);
                y += dy - 6;
            }

            y += 20;
            canvas.drawLine(x, y, x + 200, y, text);
            y += dy - 10;
            canvas.drawText("Firma del Médico", x, y, text);

            doc.finishPage(page);

            File outDir = requireContext().getExternalFilesDir(null);
            if (outDir == null) outDir = requireContext().getFilesDir();
            File pdfFile = new File(outDir, "receta_" + System.currentTimeMillis() + ".pdf");

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                doc.writeTo(fos);
            }
            doc.close();

            Toast.makeText(getContext(), "PDF generado correctamente", Toast.LENGTH_SHORT).show();
            openPdfFile(pdfFile);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir con"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se encontró visor PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private String[] wrap(String text, int max) {
        if (TextUtils.isEmpty(text)) return new String[]{"—"};
        text = text.replace("\n", " ");
        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() + w.length() + 1 > max) {
                lines.add(sb.toString());
                sb.setLength(0);
            }
            if (sb.length() > 0) sb.append(' ');
            sb.append(w);
        }
        if (sb.length() > 0) lines.add(sb.toString());
        return lines.toArray(new String[0]);
    }
}
