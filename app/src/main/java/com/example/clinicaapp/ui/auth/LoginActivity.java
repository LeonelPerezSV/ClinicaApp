package com.example.clinicaapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clinicaapp.MainActivity;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUser, edtPass;
    private Button btnLogin;
    private Spinner spinnerUserType;
    private TextView txtRegisterLink;

    private String selectedUserType = "Paciente";
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
        if (session.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }





        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        txtRegisterLink = findViewById(R.id.txtRegisterLink);

        db = AppDatabase.getInstance(this);

        // Configurar el spinner de tipo de usuario
        String[] tipos = {"Paciente", "Doctor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);

        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserType = "Paciente";
            }
        });

        // Acción de inicio de sesión
        btnLogin.setOnClickListener(v -> handleLogin());

        // Redirigir al registro
        txtRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void handleLogin() {
        String username = edtUser.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // ✅ Usar UN único método de DAO
            User user = db.userDao().login(username, password);

            if (user == null) {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Evitar NullPointer si vienen nulos
            String safeName = (user.getFullName() == null || user.getFullName().isEmpty())
                    ? user.getUsername()
                    : user.getFullName();
            String safeType = (user.getUserType() == null || user.getUserType().isEmpty())
                    ? "Paciente"
                    : user.getUserType();

            // Guardar sesión activa
            SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
            session.edit().putBoolean("logged_in", true).apply();

            // Guardar tipo y nombre para Home/Drawer
            SharedPreferences prefs = getSharedPreferences("ClinicaAppPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("user_type", safeType)
                    .putString("user_name", safeName)
                    .apply();

            Toast.makeText(this, "Bienvenido " + safeName, Toast.LENGTH_SHORT).show();

            // Ir a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            // ✅ Si algo raro pasa (schema antiguo, etc.), no se cae la app:
            Toast.makeText(this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
