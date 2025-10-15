package com.example.clinicaapp.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.clinicaapp.R;
import com.example.clinicaapp.ui.auth.LoginActivity;

public class SettingsFragment extends Fragment {

    private Switch switchTheme;
    private Button btnClearSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchTheme = view.findViewById(R.id.switchTheme);
        btnClearSession = view.findViewById(R.id.btnClearSession);

        // Cargar preferencia de tema
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", getContext().MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        switchTheme.setChecked(darkMode);

        // Aplicar tema actual
        AppCompatDelegate.setDefaultNightMode(darkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Listener del switch de tema
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

            Toast.makeText(getContext(),
                    isChecked ? "Modo oscuro activado" : "Modo claro activado",
                    Toast.LENGTH_SHORT).show();
        });

        // Cerrar sesión
        btnClearSession.setOnClickListener(v -> {
            // Limpiar datos de sesión
            SharedPreferences sessionPrefs = requireActivity().getSharedPreferences("session", getContext().MODE_PRIVATE);
            sessionPrefs.edit().clear().apply();
            prefs.edit().clear().apply();

            Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            // Redirigir al login
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
