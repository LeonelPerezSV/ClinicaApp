package com.example.clinicaapp.ui.appointments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.databinding.FragmentAppointmentFormBinding;
import com.example.clinicaapp.viewmodel.AppointmentViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.Executors;

public class AppointmentFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";

    public static AppointmentFormFragment newInstance(int id) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, id);
        AppointmentFormFragment f = new AppointmentFormFragment();
        f.setArguments(b);
        return f;
    }

    private FragmentAppointmentFormBinding binding;
    private AppointmentViewModel viewModel;
    private Integer currentId = null;

    // ids del usuario logueado
    private long loggedUserId;
    private boolean isDoctor;

    // 🔹 mapa para vincular nombre mostrado con ID real del paciente
    private Map<String, Integer> patientMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppointmentFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);

        // ⚙️ Sesión / rol actual
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String type = prefs.getString("user_type", "Paciente");
        loggedUserId = prefs.getLong("user_id", 0);
        isDoctor = "Doctor".equalsIgnoreCase(type);

        // 🔹 Cargar dropdown de pacientes
        cargarPacientes();

        // 🔹 Bloquear campos según tipo de usuario
        if (isDoctor) {
            binding.inputDoctorId.setText(String.valueOf(loggedUserId));
            binding.inputDoctorId.setEnabled(false);
        } else {
            binding.inputPatientId.setText(String.valueOf(loggedUserId));
            binding.inputPatientId.setEnabled(false);
        }

        // 🔹 Cargar datos si es edición
        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            currentId = id;
            viewModel.getById(id).observe(getViewLifecycleOwner(), a -> {
                if (a != null) {
                    binding.inputPatientId.setText(String.valueOf(a.getPatientId()));
                    binding.inputDoctorId.setText(String.valueOf(a.getDoctorId()));
                    binding.inputDate.setText(a.getDate());
                    binding.inputTime.setText(a.getTime());
                    binding.inputReason.setText(a.getReason());
                    binding.inputStatus.setText(a.getStatus());
                    binding.btnDelete.setVisibility(View.VISIBLE);
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> guardarCita());
        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnExportPdf.setOnClickListener(v -> generarPDF());

        // ✅ Eliminar con confirmación
        binding.btnDelete.setOnClickListener(v -> {
            if (currentId == null) return;
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar cita")
                    .setMessage("¿Deseas eliminar esta cita?")
                    .setPositiveButton("Eliminar", (d, w) -> eliminarCita(currentId))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    /**
     * Carga dinámica de pacientes desde Room y configura el dropdown
     */
    private void cargarPacientes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Patient> patients = db.patientDao().getAll().getValue();

            // ⚠️ Puede que LiveData no devuelva aún la lista, así que lo forzamos con consulta directa
            if (patients == null || patients.isEmpty()) {
                patients = db.patientDao().getAllPatientsList();
            }

            List<String> nombres = new ArrayList<>();
            patientMap.clear();

            for (Patient p : patients) {
                String display = "#" + p.getId() + " - " + p.getFirstName() + " " + p.getLastName();
                nombres.add(display);
                patientMap.put(display, p.getId());
            }

            List<String> finalNombres = nombres;
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        finalNombres
                );
                binding.inputPatientId.setAdapter(adapter);
            });
        });
    }

    private void guardarCita() {
        String patientStr = binding.inputPatientId.getText().toString().trim();
        String doctorStr  = binding.inputDoctorId.getText().toString().trim();
        String dateStr    = binding.inputDate.getText().toString().trim();
        String timeStr    = binding.inputTime.getText().toString().trim();

        if (TextUtils.isEmpty(patientStr) || TextUtils.isEmpty(doctorStr) ||
                TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(timeStr)) {
            Toast.makeText(getContext(), "Paciente, Doctor, Fecha y Hora son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Appointment a = new Appointment();
        if (currentId != null) a.setId(currentId);

        // 🔒 Forzar IDs correctos según rol
        if (isDoctor) {
            a.setDoctorId((int) loggedUserId);

            // obtiene ID real del paciente seleccionado
            Integer selectedPatientId = patientMap.get(patientStr);
            if (selectedPatientId == null) {
                Toast.makeText(getContext(), "Seleccione un paciente válido", Toast.LENGTH_SHORT).show();
                return;
            }
            a.setPatientId(selectedPatientId);

        } else {
            a.setPatientId((int) loggedUserId);
            a.setDoctorId(Integer.parseInt(doctorStr));
        }

        a.setDate(dateStr);
        a.setTime(timeStr);
        a.setReason(binding.inputReason.getText().toString().trim());
        a.setStatus(binding.inputStatus.getText().toString().trim());

        if (currentId == null) {
            viewModel.insert(a);
            Toast.makeText(getContext(), "Cita creada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(a);
            Toast.makeText(getContext(), "Cita actualizada", Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void eliminarCita(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.appointmentDao().deleteById(id);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        });
    }

    private void generarPDF() {
        if (currentId == null) {
            Toast.makeText(requireContext(), "Guarda la cita antes de exportar a PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Appointment a = db.appointmentDao().getByIdDirect(currentId);
            if (a != null) requireActivity().runOnUiThread(() -> exportarPDF(a));
        });
    }

    private void exportarPDF(Appointment a) {
        try {
            File pdfFile = new File(requireContext().getExternalFilesDir(null), "Cita_" + a.getId() + ".pdf");
            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
            PdfDocument.Page page = doc.startPage(info);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12);

            int y = 30;
            canvas.drawText("CLÍNICA APP - CITA", 20, y, paint); y += 30;
            canvas.drawText("Doctor ID: " + a.getDoctorId(), 20, y, paint); y += 20;
            canvas.drawText("Paciente ID: " + a.getPatientId(), 20, y, paint); y += 20;
            canvas.drawText("Fecha: " + a.getDate(), 20, y, paint); y += 20;
            canvas.drawText("Hora: " + a.getTime(), 20, y, paint); y += 20;
            canvas.drawText("Estado: " + a.getStatus(), 20, y, paint); y += 20;
            canvas.drawText("Motivo: " + (a.getReason() == null ? "-" : a.getReason()), 20, y, paint);

            doc.finishPage(page);
            try (FileOutputStream out = new FileOutputStream(pdfFile)) {
                doc.writeTo(out);
            }
            doc.close();

            Toast.makeText(requireContext(), "PDF generado: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
