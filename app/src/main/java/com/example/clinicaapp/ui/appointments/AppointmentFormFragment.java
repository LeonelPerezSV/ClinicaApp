package com.example.clinicaapp.ui.appointments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.databinding.FragmentAppointmentFormBinding;
import com.example.clinicaapp.viewmodel.AppointmentViewModel;
import com.example.clinicaapp.viewmodel.DoctorViewModel;
import com.example.clinicaapp.viewmodel.PatientViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AppointmentFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";
    private static final String ARG_READ_ONLY = "readOnly";

    private FragmentAppointmentFormBinding binding;
    private AppointmentViewModel appointmentViewModel;
    private PatientViewModel patientViewModel;
    private DoctorViewModel doctorViewModel;

    private Integer currentId = null;
    private Doctor loggedDoctor = null;

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

    public static AppointmentFormFragment newInstance(int id) { return newInstance(id, false); }
    public static AppointmentFormFragment newInstance(int id, boolean readOnly) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putBoolean(ARG_READ_ONLY, readOnly);
        AppointmentFormFragment fragment = new AppointmentFormFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppointmentFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appointmentViewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        setupUI();
        setupObservers();

        if (getArguments() != null && getArguments().containsKey(ARG_ID)) {
            int id = getArguments().getInt(ARG_ID, -1);
            if (id != -1) {
                currentId = id;
                loadExistingAppointment(id);
            }
        }
    }

    private void setupObservers() {
        patientViewModel.getAll().observe(getViewLifecycleOwner(), patients -> {
            if (patients == null) return;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, patients.stream().map(p -> p.getId() + " - " + p.getName()).collect(Collectors.toList()));
            binding.spinnerPatient.setAdapter(adapter);
            if (currentId != null) loadExistingAppointment(currentId);
        });

        doctorViewModel.getAll().observe(getViewLifecycleOwner(), doctors -> {
            if (doctors == null) return;
            SharedPreferences prefs = requireContext().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
            String userType = prefs.getString("user_type", "");
            String userName = prefs.getString("user_name", "");

            if ("Doctor".equalsIgnoreCase(userType)) {
                for (Doctor d : doctors) {
                    if (d.getName().trim().equalsIgnoreCase(userName.trim())) {
                        loggedDoctor = d;
                        break;
                    }
                }
                if (loggedDoctor != null) {
                    binding.layoutSpinnerDoctor.setVisibility(View.GONE);
                } else {
                    setupDoctorSpinner(doctors);
                }
            } else {
                setupDoctorSpinner(doctors);
            }
            if (currentId != null) loadExistingAppointment(currentId);
        });
    }

    private void setupDoctorSpinner(List<Doctor> doctors) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, doctors.stream().map(d -> d.getId() + " - " + d.getName()).collect(Collectors.toList()));
        binding.spinnerDoctor.setAdapter(adapter);
    }

    private void setupUI() {
        // Listeners de fecha, hora y estado restaurados para el formulario
        binding.inputDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection));
                binding.inputDate.setText(d);
            });
            picker.show(getParentFragmentManager(), "datePicker");
        });

        binding.inputTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build();
            picker.addOnPositiveButtonClickListener(view1 ->
                    binding.inputTime.setText(String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute()))
            );
            picker.show(getParentFragmentManager(), "timePicker");
        });

        String[] estados = {"Pendiente", "Completada", "Cancelada"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        binding.inputStatus.setAdapter(statusAdapter);

        binding.btnSave.setOnClickListener(v -> saveAppointment());
        binding.btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnExportPdf.setOnClickListener(v -> checkPermissionAndExport());

        if (getArguments() != null && getArguments().getBoolean(ARG_READ_ONLY, false)) {
            binding.spinnerPatient.setEnabled(false);
            binding.spinnerDoctor.setEnabled(false);
            binding.inputDate.setEnabled(false);
            binding.inputTime.setEnabled(false);
            binding.inputStatus.setEnabled(false);
            binding.inputReason.setEnabled(false);
            binding.btnSave.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
        }
    }

    private void loadExistingAppointment(int id) {
        appointmentViewModel.getById(id).observe(getViewLifecycleOwner(), a -> {
            if (a == null) return;
            currentId = a.getId();
            binding.inputDate.setText(a.getDate());
            binding.inputTime.setText(a.getTime());
            binding.inputStatus.setText(a.getStatus());
            binding.inputReason.setText(a.getReason());

            selectDropdownValue(binding.spinnerPatient, a.getPatientId());
            if (loggedDoctor == null) {
                selectDropdownValue(binding.spinnerDoctor, a.getDoctorId());
            }

            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(v -> {
                appointmentViewModel.delete(a);
                Toast.makeText(getContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
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

    private void saveAppointment() {
        String patientString = binding.spinnerPatient.getText().toString();
        String date = binding.inputDate.getText().toString();
        String time = binding.inputTime.getText().toString();
        String status = binding.inputStatus.getText().toString();

        int doctorId;
        if (loggedDoctor != null) {
            doctorId = loggedDoctor.getId();
        } else {
            String doctorString = binding.spinnerDoctor.getText().toString();
            if (doctorString.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, seleccione un doctor", Toast.LENGTH_SHORT).show();
                return;
            }
            doctorId = Integer.parseInt(doctorString.split(" - ")[0]);
        }

        if (patientString.isEmpty() || date.isEmpty() || time.isEmpty() || status.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = Integer.parseInt(patientString.split(" - ")[0]);

        Appointment appointment = new Appointment();
        if (currentId != null) {
            appointment.setId(currentId);
        }
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setStatus(status);
        appointment.setReason(binding.inputReason.getText().toString());

        if (currentId == null) {
            appointmentViewModel.insert(appointment);
            Toast.makeText(getContext(), "Cita creada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            appointmentViewModel.update(appointment);
            Toast.makeText(getContext(), "Cita actualizada", Toast.LENGTH_SHORT).show();
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
        String time = binding.inputTime.getText().toString();
        String status = binding.inputStatus.getText().toString();
        String reason = binding.inputReason.getText().toString();

        if (patientString.isEmpty()) {
            Toast.makeText(getContext(), "No se puede exportar sin un paciente seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = Integer.parseInt(patientString.split(" - ")[0]);
        String patientName = patientString.split(" - ")[1];

        int doctorId;
        String doctorName;
        if (loggedDoctor != null) {
            doctorId = loggedDoctor.getId();
            doctorName = loggedDoctor.getName();
        } else {
            String doctorString = binding.spinnerDoctor.getText().toString();
            if(doctorString.isEmpty()) {
                Toast.makeText(getContext(), "No se puede exportar sin un doctor seleccionado", Toast.LENGTH_SHORT).show();
                return;
            }
            doctorId = Integer.parseInt(doctorString.split(" - ")[0]);
            doctorName = doctorString.split(" - ")[1];
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

            File outDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(outDir, "cita_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                doc.writeTo(fos);
            }
            doc.close();

            openPdfFile(pdfFile);

        } catch (IOException e) {
            Log.e("PdfExport", "Error al generar PDF", e);
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
