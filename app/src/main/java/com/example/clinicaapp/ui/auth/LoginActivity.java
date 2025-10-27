package com.example.clinicaapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clinicaapp.MainActivity;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;
import com.example.clinicaapp.data.repo.FirebaseSyncRepository;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUser, edtPass;
    private Button btnLogin;
    private Spinner spinnerUserType;
    private TextView txtRegisterLink;
    private CheckBox cbRemember;
    private String selectedUserType = "Paciente";
    private AppDatabase db;

    // Validaciones
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static boolean isValidEmail(String s) {
        return s != null && EMAIL_PATTERN.matcher(s).matches();
    }

    private static boolean isValidPassword(String s) {
        return s != null && s.length() >= 8 && s.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$");
    }

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
        cbRemember = findViewById(R.id.cbRemember);

        db = AppDatabase.getInstance(this);

        // Configuración del Spinner
        String[] tipos = {"Paciente", "Doctor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);
        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserType = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedUserType = "Paciente"; }
        });

        // Recordarme: restaurar datos si existen
        SharedPreferences prefs = getSharedPreferences("ClinicaAppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("remember_me", false)) {
            cbRemember.setChecked(true);
            edtUser.setText(prefs.getString("remember_user", ""));
            edtPass.setText(prefs.getString("remember_pass", ""));
        }

        btnLogin.setOnClickListener(v -> handleLogin());
        txtRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void handleLogin() {
        String username = edtUser.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        // Validaciones
        if (!isValidEmail(username)) {
            Toast.makeText(this, "Correo inválido. Ejemplo: usuario@dominio.com", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres alfanuméricos.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            User user = db.userDao().login(username, password);
            if (user == null) {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar sesión
            SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
            session.edit().putBoolean("logged_in", true).apply();

            SharedPreferences prefs = getSharedPreferences("ClinicaAppPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("user_type", user.getUserType() == null ? "Paciente" : user.getUserType())
                    .putString("user_name", user.getFullName() == null ? user.getUsername() : user.getFullName())
                    .putLong("user_id", user.getId())
                    .apply();

            // Recordarme
            SharedPreferences.Editor e = prefs.edit();
            if (cbRemember.isChecked()) {
                e.putBoolean("remember_me", true);
                e.putString("remember_user", username);
                e.putString("remember_pass", password);
            } else {
                e.remove("remember_me");
                e.remove("remember_user");
                e.remove("remember_pass");
            }
            e.apply();



            startActivity(new Intent(this, MainActivity.class));
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


}
