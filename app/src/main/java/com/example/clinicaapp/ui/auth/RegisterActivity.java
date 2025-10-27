package com.example.clinicaapp.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;
import com.example.clinicaapp.data.repo.FirebaseSyncRepository;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtUser, edtPass;
    private Spinner spinnerType;
    private Button btnRegister;
    private String selectedType = "Paciente";
    private AppDatabase db;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

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

        String[] tipos = {"Paciente", "Doctor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedType = "Paciente"; }
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtUser.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        // Validaciones
        if (fullName.length() < 7) {
            Toast.makeText(this, "El nombre completo debe tener al menos 7 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido. Ejemplo: usuario@dominio.com", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(this, "La contraseña debe tener mínimo 8 caracteres alfanuméricos.", Toast.LENGTH_LONG).show();
            return;
        }

        User existing = db.userDao().findByUsername(email);
        if (existing != null) {
            Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario
        User user = new User(fullName, email, password, selectedType);
        long userId = db.userDao().insert(user);
        user.setId((int) userId);

        // Sincronizar con Firestore
        FirebaseSyncRepository syncRepo = new FirebaseSyncRepository(this);
        syncRepo.syncUserToFirestore(user);

        // Crear perfil automático
        if ("Doctor".equalsIgnoreCase(selectedType)) {
            try {
                com.example.clinicaapp.data.repo.DoctorRepository doctorRepo =
                        new com.example.clinicaapp.data.repo.DoctorRepository(this);
                com.example.clinicaapp.data.entities.Doctor doctor =
                        new com.example.clinicaapp.data.entities.Doctor(fullName, "General", email, "0000-0000");
                doctorRepo.insert(doctor);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creando perfil de doctor", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                com.example.clinicaapp.data.repo.PatientRepository patientRepo =
                        new com.example.clinicaapp.data.repo.PatientRepository(this);
                String[] parts = fullName.split(" ", 2);
                String first = parts.length > 0 ? parts[0] : fullName;
                String last = parts.length > 1 ? parts[1] : "";
                com.example.clinicaapp.data.entities.Patient p =
                        new com.example.clinicaapp.data.entities.Patient(first, last, email, "0000-0000", user.getId());
                patientRepo.insert(p);

                new android.os.Handler().postDelayed(() -> {
                    com.example.clinicaapp.data.entities.Patient lastPatient =
                            db.patientDao().getAllPatientsList().get(db.patientDao().getAllPatientsList().size() - 1);
                    if (lastPatient != null) {
                        com.example.clinicaapp.data.repo.MedicalRecordRepository recordRepo =
                                new com.example.clinicaapp.data.repo.MedicalRecordRepository(this);
                        com.example.clinicaapp.data.entities.MedicalRecord record =
                                new com.example.clinicaapp.data.entities.MedicalRecord(
                                        lastPatient.getId(),
                                        "Sin diagnóstico inicial",
                                        "Sin alergias registradas",
                                        "Sin notas médicas");
                        recordRepo.insert(record);
                    }
                }, 400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
        finish();
    }
}
