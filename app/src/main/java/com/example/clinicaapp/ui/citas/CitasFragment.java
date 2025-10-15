package com.example.clinicaapp.ui.citas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.clinicaapp.databinding.FragmentCitasBinding;

public class CitasFragment extends Fragment {

    private FragmentCitasBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCitasBinding.inflate(inflater, container, false);
        binding.txtTitle.setText("Gestión de Citas Médicas 🗓️");
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
