package com.example.clinicaapp.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clinicaapp.MainActivity;
import com.example.clinicaapp.R;
import com.example.clinicaapp.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pequeño delay opcional para ver el logo
        getWindow().getDecorView().postDelayed(() -> {
            SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
            boolean logged = session.getBoolean("logged_in", false);

            Intent next = new Intent(this, logged ? MainActivity.class : LoginActivity.class);
            startActivity(next);
            finish();
        }, 2000); // 2 segundos de transición
    }
}
