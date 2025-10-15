package com.example.clinicaapp.ui.doctors;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.databinding.FragmentDoctorFormBinding;
import com.example.clinicaapp.viewmodel.DoctorViewModel;

public class DoctorFormFragment extends Fragment {

    private static final String ARG_ID = "arg_id";
    public static DoctorFormFragment newInstance(int id) {
        Bundle b = new Bundle(); b.putInt(ARG_ID, id);
        DoctorFormFragment f = new DoctorFormFragment(); f.setArguments(b); return f;
    }

    private FragmentDoctorFormBinding binding;
    private DoctorViewModel viewModel;
    private Integer currentId = null;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDoctorFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        int id = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
        if (id != -1) {
            currentId = id;
            viewModel.getById(id).observe(getViewLifecycleOwner(), d -> {
                if (d != null) {
                    binding.inputFirstName.setText(d.getFirstName());
                    binding.inputLastName.setText(d.getLastName());
                    binding.inputSpecialty.setText(d.getSpecialty());
                    binding.inputEmail.setText(d.getEmail());
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> {
            String first = binding.inputFirstName.getText().toString().trim();
            String last = binding.inputLastName.getText().toString().trim();
            String spec = binding.inputSpecialty.getText().toString().trim();
            String email = binding.inputEmail.getText().toString().trim();

            if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last)) {
                Toast.makeText(getContext(), "Nombre y apellido son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            Doctor d = new Doctor();
            if (currentId != null) d.setId(currentId);
            d.setFirstName(first);
            d.setLastName(last);
            d.setSpecialty(spec);
            d.setEmail(email);

            if (currentId == null) {
                viewModel.insert(d);
                Toast.makeText(getContext(), "Doctor creado", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.update(d);
                Toast.makeText(getContext(), "Doctor actualizado", Toast.LENGTH_SHORT).show();
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }
}
