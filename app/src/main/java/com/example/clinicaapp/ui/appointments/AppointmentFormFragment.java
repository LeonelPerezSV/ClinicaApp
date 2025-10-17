package com.example.clinicaapp.ui.appointments;

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

import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.repo.DoctorRepository;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.databinding.FragmentAppointmentFormBinding;
import com.example.clinicaapp.viewmodel.AppointmentViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.content.Intent;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.Locale;
import java.util.stream.Collectors;

public class AppointmentFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";

    // ✅ Versión original (para compatibilidad)
    public static AppointmentFormFragment newInstance(int id) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, id);
        AppointmentFormFragment f = new AppointmentFormFragment();
        f.setArguments(b);
        return f;
    }

    // ✅ Nueva versión sobrecargada (corrige error en AppointmentListFragment)
    public static AppointmentFormFragment newInstance(int id, boolean readOnly) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, id);
        b.putBoolean("readOnly", readOnly);
        AppointmentFormFragment f = new AppointmentFormFragment();
        f.setArguments(b);
        return f;
    }

    private FragmentAppointmentFormBinding binding;
    private AppointmentViewModel viewModel;
    private Integer currentId = null;
    private Doctor loggedDoctor = null;
    private Integer preselectedPatientId = null;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppointmentFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);

        // Detectar usuario logueado
        SharedPreferences prefs = requireContext().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String userType = prefs.getString("user_type", "");
        String userName = prefs.getString("user_name", "");

        // 🔹 Control de visibilidad de botones según tipo de usuario
        boolean isDoctor = "Doctor".equalsIgnoreCase(userType);
        if (!isDoctor) {
            binding.btnDelete.setVisibility(View.GONE); // Oculta el botón eliminar
            binding.btnSave.setVisibility(View.GONE);   // Opcional: también ocultar guardar
            binding.btnExportPdf.setVisibility(View.VISIBLE); // Mantener visible PDF
            binding.btnCancel.setVisibility(View.VISIBLE);    // Mantener visible cancelar
        }


        if ("Doctor".equalsIgnoreCase(userType)) {
            try {
                DoctorRepository repo = new DoctorRepository(requireContext());
                List<Doctor> all = repo.getAllList();
                for (Doctor d : all) {
                    if (d.getName().trim().equalsIgnoreCase(userName.trim())) {
                        loggedDoctor = d;
                        break;
                    }
                }

                if (loggedDoctor != null) {
                    binding.spinnerDoctor.setVisibility(View.GONE);
                    TextView fixedDoctor = new TextView(requireContext());
                    fixedDoctor.setText("👨‍⚕️ Doctor: " + loggedDoctor.getName());
                    fixedDoctor.setTextSize(16f);
                    fixedDoctor.setPadding(0, 16, 0, 16);
                    ((ViewGroup) binding.spinnerDoctor.getParent()).addView(fixedDoctor, 2);
                } else {
                    Toast.makeText(getContext(), "No se encontró el perfil del doctor", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al cargar el doctor", Toast.LENGTH_SHORT).show();
            }
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Patient> patients = db.patientDao().getAllPatientsList();
            List<Doctor> doctors = db.doctorDao().getAllDoctorsList();

            requireActivity().runOnUiThread(() -> {
                // Pacientes
                ArrayAdapter<String> pAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        patients.stream().map(p -> p.getId() + " - " + p.getName()).collect(Collectors.toList())
                );
                pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerPatient.setAdapter(pAdapter);

                // Doctores
                ArrayAdapter<String> dAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        doctors.stream().map(d -> d.getId() + " - " + d.getName()).collect(Collectors.toList())
                );
                dAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerDoctor.setAdapter(dAdapter);

                if (getArguments() != null && getArguments().containsKey(ARG_ID))
                    cargarCitaExistente(getArguments().getInt(ARG_ID));
            });
        });

        // Estados
        String[] estados = {"Pendiente", "Completada", "Cancelada"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, estados);
        binding.inputStatus.setAdapter(statusAdapter);
        binding.inputStatus.setEnabled(loggedDoctor != null);

        // Fecha
        binding.inputDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccionar fecha")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection));
                binding.inputDate.setText(d);
            });
            picker.show(getParentFragmentManager(), "datePicker");
        });

        // Hora
        binding.inputTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(9)
                    .setMinute(0)
                    .setTitleText("Seleccionar hora")
                    .build();

            picker.addOnPositiveButtonClickListener(view1 ->
                    binding.inputTime.setText(String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute()))
            );
            picker.show(getParentFragmentManager(), "timePicker");
        });

        binding.btnSave.setOnClickListener(v -> guardarCita());
        binding.btnExportPdf.setOnClickListener(v -> exportPdf());
        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // ✅ Aplicar modo lectura si viene de paciente
        boolean readOnly = getArguments() != null && getArguments().getBoolean("readOnly", false);
        if (readOnly) {
            binding.btnSave.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
            binding.spinnerPatient.setEnabled(false);
            binding.spinnerDoctor.setEnabled(false);
            binding.inputDate.setEnabled(false);
            binding.inputTime.setEnabled(false);
            binding.inputStatus.setEnabled(false);
            binding.inputReason.setEnabled(false);

            // 👇 Mantener visible solo el botón de PDF y cancelar
            binding.btnExportPdf.setVisibility(View.VISIBLE);
            binding.btnCancel.setVisibility(View.VISIBLE);
        }
    }

    private void cargarCitaExistente(int id) {
        viewModel.getById(id).observe(getViewLifecycleOwner(), a -> {
            if (a != null) {
                isEditMode = true;
                currentId = id;
                preselectedPatientId = a.getPatientId();

                binding.inputDate.setText(a.getDate());
                binding.inputTime.setText(a.getTime());
                binding.inputReason.setText(a.getReason());
                binding.inputStatus.setText(a.getStatus());

                trySelectPatientInSpinner();
                binding.spinnerDoctor.setEnabled(false);

                // 🔹 Mostrar eliminar solo si es doctors
                SharedPreferences prefs = requireContext().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
                boolean isDoctor = "Doctor".equalsIgnoreCase(prefs.getString("user_type", ""));
                binding.btnDelete.setVisibility(isDoctor ? View.VISIBLE : View.GONE);


                binding.btnDelete.setOnClickListener(v -> {
                    viewModel.deleteById(a.getId());
                    Toast.makeText(getContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
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

    private void guardarCita() {
        String selectedPatient;
        if (isEditMode && preselectedPatientId != null) {
            selectedPatient = preselectedPatientId + " - bloqueado";
        } else {
            selectedPatient = (String) binding.spinnerPatient.getSelectedItem();
        }

        if (selectedPatient == null || TextUtils.isEmpty(binding.inputDate.getText()) ||
                TextUtils.isEmpty(binding.inputTime.getText())) {
            Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = Integer.parseInt(selectedPatient.split(" - ")[0]);
        int doctorId;

        if (loggedDoctor != null) {
            doctorId = loggedDoctor.getId();
        } else {
            String selectedDoctor = (String) binding.spinnerDoctor.getSelectedItem();
            if (selectedDoctor == null) {
                Toast.makeText(getContext(), "Seleccione un doctor", Toast.LENGTH_SHORT).show();
                return;
            }
            doctorId = Integer.parseInt(selectedDoctor.split(" - ")[0]);
        }

        Appointment a = new Appointment();
        if (currentId != null) a.setId(currentId);
        a.setDoctorId(doctorId);
        a.setPatientId(patientId);
        a.setDate(binding.inputDate.getText().toString().trim());
        a.setTime(binding.inputTime.getText().toString().trim());
        a.setStatus(binding.inputStatus.getText().toString().trim());
        a.setReason(binding.inputReason.getText().toString().trim());

        if (currentId == null) {
            viewModel.insert(a);
            Toast.makeText(getContext(), "Cita creada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(a);
            Toast.makeText(getContext(), "Cita actualizada", Toast.LENGTH_SHORT).show();
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
        String time = binding.inputTime.getText().toString().trim();
        String status = binding.inputStatus.getText().toString().trim();
        String reason = binding.inputReason.getText().toString().trim();

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

            canvas.drawText("CLINICAPP – Detalle de Cita Médica", x, y, title);
            y += dy;
            canvas.drawLine(x, y, info.getPageWidth() - x, y, text);
            y += dy;

            canvas.drawText("Fecha: " + date + "  Hora: " + time, x, y, text);
            y += dy;
            canvas.drawText("Paciente: " + patientId + " - " + patientName, x, y, text);
            y += dy;
            canvas.drawText("Doctor: " + doctorId + " - " + doctorName, x, y, text);
            y += dy;
            canvas.drawText("Estado: " + status, x, y, text);
            y += dy;

            canvas.drawText("Motivo de la Cita:", x, y, title);
            y += dy;
            canvas.drawText(TextUtils.isEmpty(reason) ? "—" : reason, x, y, text);

            doc.finishPage(page);

            File outDir = requireContext().getExternalFilesDir(null);
            File pdfFile = new File(outDir, "cita_" + System.currentTimeMillis() + ".pdf");
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
