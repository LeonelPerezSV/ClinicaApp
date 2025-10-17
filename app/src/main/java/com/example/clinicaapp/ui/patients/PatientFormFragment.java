package com.example.clinicaapp.ui.patients;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.text.TextUtils;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.databinding.FragmentPatientFormBinding;
import com.example.clinicaapp.viewmodel.PatientViewModel;
import java.util.concurrent.Executors;

public class PatientFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";

    public static PatientFormFragment newInstance(int id) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, id);
        PatientFormFragment f = new PatientFormFragment();
        f.setArguments(b);
        return f;
    }

    private FragmentPatientFormBinding binding;
    private PatientViewModel viewModel;
    private Integer currentId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPatientFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            currentId = id;
            viewModel.getById(id).observe(getViewLifecycleOwner(), p -> {
                if (p != null) {
                    binding.inputFirstName.setText(p.getFirstName());
                    binding.inputLastName.setText(p.getLastName());
                    binding.inputEmail.setText(p.getEmail());
                    binding.inputPhone.setText(p.getPhone());
                    binding.inputCreatedAt.setText(p.getCreatedAt());
                    binding.btnDelete.setVisibility(View.VISIBLE);
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> guardarPaciente());
        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnDelete.setOnClickListener(v -> {
            if (currentId == null) return;
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Paciente")
                    .setMessage("¿Deseas eliminar este paciente?")
                    .setPositiveButton("Eliminar", (d, w) -> eliminarPaciente(currentId))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void guardarPaciente() {
        String first = binding.inputFirstName.getText().toString().trim();
        String last = binding.inputLastName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String phone = binding.inputPhone.getText().toString().trim();

        // Limpiar errores previos
        binding.layoutFirstName.setError(null);
        binding.layoutLastName.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPhone.setError(null);

        boolean valid = true;

        if (TextUtils.isEmpty(first)) {
            binding.layoutFirstName.setError("Ingrese el nombre");
            valid = false;
        }
        if (TextUtils.isEmpty(last)) {
            binding.layoutLastName.setError("Ingrese el apellido");
            valid = false;
        }
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Correo no válido");
            valid = false;
        }
        if (!TextUtils.isEmpty(phone) && !Patterns.PHONE.matcher(phone).matches()) {
            binding.layoutPhone.setError("Teléfono no válido");
            valid = false;
        }

        if (!valid) return;

        Patient p = new Patient();
        if (currentId != null) p.setId(currentId);
        p.setFirstName(first);
        p.setLastName(last);
        p.setEmail(email);
        p.setPhone(phone);

        if (currentId == null) {
            viewModel.insert(p);
            Toast.makeText(getContext(), "Paciente creado exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.update(p);
            Toast.makeText(getContext(), "Paciente actualizado", Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void eliminarPaciente(int id) {
        // Obtén el paciente sincrónicamente y usa la cascada del ViewModel
        new Thread(() -> {
            Patient p = AppDatabase.getInstance(requireContext()).patientDao().findById(id);
            if (p != null) {
                requireActivity().runOnUiThread(() -> {
                    viewModel.deletePatientCascade(p);
                    Toast.makeText(requireContext(), "Paciente y datos asociados eliminados", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            } else {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Paciente no encontrado", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

}
