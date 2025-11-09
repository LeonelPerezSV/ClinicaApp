package com.example.clinicaapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clinicaapp.MainActivity;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;
import com.example.clinicaapp.data.repo.FirebaseSyncRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUser, edtPass;
    private Button btnLogin;
    private Spinner spinnerUserType;
    private TextView txtRegisterLink;
    private CheckBox cbRemember;
    private String selectedUserType = "Paciente";
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private FirebaseSyncRepository syncRepo;

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

        mAuth = FirebaseAuth.getInstance();
        syncRepo = new FirebaseSyncRepository(this);

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
        String username = edtUser.getText().toString().trim().toLowerCase();
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

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "signInWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        // Sincronizar los datos del usuario desde Firestore a la base de datos local
                        syncRepo.pullUserByEmail(firebaseUser.getEmail(), user -> {
                            if (user != null) {
                                runOnUiThread(() -> onLoginSuccess(user, password));
                            } else {
                                // El usuario se autenticó en Firebase, pero no se encontraron sus datos en Firestore.
                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, "No se encontraron los datos del perfil de usuario.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut(); // Cerramos la sesión de Firebase para evitar un estado inconsistente
                                });
                            }
                        });
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                        // Si falla la autenticación de Firebase, intentamos el login local como último recurso.
                        tryLocalLogin(username, password);
                    }
                });
    }

    private void tryLocalLogin(String username, String password) {
        new Thread(() -> {
            User user = db.userDao().login(username, password);
            runOnUiThread(() -> {
                if (user == null) {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    return;
                }
                onLoginSuccess(user, password);
            });
        }).start();
    }

    private void onLoginSuccess(User user, String password) {
        // Guardar sesión
        SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
        session.edit().putBoolean("logged_in", true).apply();

        // El objeto User ya está completo, no es necesario volver a consultarlo.
        SharedPreferences prefs = getSharedPreferences("ClinicaAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_type", user.getUserType() == null ? "Paciente" : user.getUserType());
        editor.putString("user_name", user.getFullName() == null ? user.getUsername() : user.getFullName());
        editor.putString("user_email", user.getUsername());
        editor.putLong("user_id", user.getId());
        editor.putString("user_phone", user.getPhone());

        if ("Doctor".equals(user.getUserType())) {
            editor.putString("user_specialty", user.getSpecialty());
        }

        if (user.getPhotoUrl() != null) {
            editor.putString("user_photo_url", user.getPhotoUrl());
        }

        // Recordarme
        if (cbRemember.isChecked()) {
            editor.putBoolean("remember_me", true);
            editor.putString("remember_user", user.getUsername());
            editor.putString("remember_pass", password);
        } else {
            editor.remove("remember_me");
            editor.remove("remember_user");
            editor.remove("remember_pass");
        }
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
