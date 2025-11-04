package com.example.clinicaapp.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    public static AppointmentFormFragment newInstance(int id) {
        return newInstance(id, false);
    }

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
            List<String> patientNames = patients.stream()
                    .map(p -> p.getId() + " - " + p.getName())
                    .collect(Collectors.toList());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, patientNames);
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
        List<String> doctorNames = doctors.stream()
                .map(d -> d.getId() + " - " + d.getName())
                .collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, doctorNames);
        binding.spinnerDoctor.setAdapter(adapter);
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

        binding.inputTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build();
            picker.addOnPositiveButtonClickListener(view1 ->
                    binding.inputTime.setText(String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute()))
            );
            picker.show(getParentFragmentManager(), "timePicker");
        });

        binding.btnSave.setOnClickListener(v -> saveAppointment());
        binding.btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        String[] estados = {"Pendiente", "Completada", "Cancelada"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        binding.inputStatus.setAdapter(statusAdapter);

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
}
