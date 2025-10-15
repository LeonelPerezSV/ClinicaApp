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

        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

        // Regresar al login
        finish();
    }
}
