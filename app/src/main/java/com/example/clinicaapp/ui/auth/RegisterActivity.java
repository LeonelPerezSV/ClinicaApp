package com.example.clinicaapp.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtUser, edtPass;
    private Spinner spinnerType;
    private Button btnRegister;

    private String selectedType = "Paciente";
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        spinnerType = findViewById(R.id.spinnerUserType);
        btnRegister = findViewById(R.id.btnRegister);

        db = AppDatabase.getInstance(this);

        // Configurar spinner de tipo de usuario
        String[] tipos = {"Paciente", "Doctor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedType = "Paciente";
            }
        });

        // Botón registrar
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String username = edtUser.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si ya existe el usuario
        User existing = db.userDao().findByUsername(username);
        if (existing != null) {
            Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear nuevo usuario
        User user = new User(fullName, username, password, selectedType);
        db.userDao().insert(user);

        // ✅ Si es doctor, crear perfil en la tabla doctors
        if ("Doctor".equalsIgnoreCase(selectedType)) {
            try {
                com.example.clinicaapp.data.repo.DoctorRepository doctorRepo =
                        new com.example.clinicaapp.data.repo.DoctorRepository(this);

                com.example.clinicaapp.data.entities.Doctor doctor =
                        new com.example.clinicaapp.data.entities.Doctor(
                                fullName,
                                "General",
                                username + "@clinicapp.com",
                                "0000-0000"
                        );

                doctorRepo.insert(doctor);
                Toast.makeText(this, "Perfil de doctor creado correctamente", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al crear perfil de doctor: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // ✅ Si es paciente, crear perfil + expediente médico
        if ("Paciente".equalsIgnoreCase(selectedType)) {
            try {
                com.example.clinicaapp.data.repo.PatientRepository patientRepo =
                        new com.example.clinicaapp.data.repo.PatientRepository(this);

                // Dividir nombre completo
                String[] parts = fullName.split(" ", 2);
                String firstName = parts.length > 0 ? parts[0] : fullName;
                String lastName = parts.length > 1 ? parts[1] : "";

                // Crear paciente
                com.example.clinicaapp.data.entities.Patient patient =
                        new com.example.clinicaapp.data.entities.Patient(
                                firstName,
                                lastName,
                                username + "@clinicapp.com",
                                "0000-0000"
                        );

                // Insertar paciente y recuperar ID generado
                patientRepo.insert(patient);

                // ⚠️ Esperar un instante a que Room lo inserte (solo necesario la primera vez)
                new android.os.Handler().postDelayed(() -> {
                    // Buscar el último paciente insertado (el más reciente)
                    com.example.clinicaapp.data.entities.Patient last =
                            db.patientDao().getAllPatientsList().get(
                                    db.patientDao().getAllPatientsList().size() - 1
                            );

                    if (last != null) {
                        // Crear expediente vinculado
                        com.example.clinicaapp.data.repo.MedicalRecordRepository recordRepo =
                                new com.example.clinicaapp.data.repo.MedicalRecordRepository(this);

                        com.example.clinicaapp.data.entities.MedicalRecord record =
                                new com.example.clinicaapp.data.entities.MedicalRecord(
                                        last.getId(),
                                        "Sin diagnóstico inicial",
                                        "Sin alergias registradas",
                                        "Sin notas médicas"
                                );

                        recordRepo.insert(record);
                        Toast.makeText(this, "Expediente médico creado automáticamente", Toast.LENGTH_SHORT).show();
                    }
                }, 500); // Pequeña pausa para asegurar inserción previa

                Toast.makeText(this, "Perfil de paciente creado correctamente", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al crear perfil de paciente o expediente: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

        // Regresar al login
        finish();
    }




}
