package com.example.clinicaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;           // <— IMPORTA View
import android.widget.ImageView;
import android.widget.TextView;    // <— IMPORTA TextView

import com.example.clinicaapp.data.repo.FirebaseSyncRepository;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clinicaapp.databinding.ActivityMainBinding;
import com.example.clinicaapp.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar sesión activa
        SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
        if (!session.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // OJO: el include en activity_main.xml tiene id appBarMain => binding.appBarMain
        setSupportActionBar(binding.appBarMain.toolbar);

        // FAB
        binding.appBarMain.fab.setOnClickListener(view ->
                Snackbar.make(view, "Acción rápida disponible", Snackbar.LENGTH_LONG)
                        .setAction("Ok", null)
                        .setAnchorView(R.id.fab)
                        .show()
        );

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navView = binding.navView;



        // Top-level destinations: usa los IDs que EXISTEN en tu nav_graph (según tu proyecto en español)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_citas,
                R.id.nav_pacientes,
                R.id.nav_recetas,
                R.id.nav_expediente,
                R.id.nav_settings
        )
                .setOpenableLayout(drawer).build();

        // Header dinámico
        View headerView = navView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.navUserName);
        TextView navUserRole = headerView.findViewById(R.id.navUserRole);
        ImageView navUserImage = headerView.findViewById(R.id.navUserImage);

        SharedPreferences prefs = getSharedPreferences("ClinicaAppPrefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Usuario");
        String userType = prefs.getString("user_type", "Paciente");

        navUserName.setText(userName);
        navUserRole.setText(userType);

// (Opcional futuro) Cargar imagen real si existe ruta guardada
// String imageUri = prefs.getString("user_image", null);
// if (imageUri != null) navUserImage.setImageURI(Uri.parse(imageUri));


        // Mostrar/ocultar grupos de menú por rol
        Menu menu = navView.getMenu();
        if ("Paciente".equals(userType)) {
            menu.setGroupVisible(R.id.group_patient, true);
            menu.setGroupVisible(R.id.group_doctor, false);
        } else if ("Doctor".equals(userType)) {
            menu.setGroupVisible(R.id.group_patient, false);
            menu.setGroupVisible(R.id.group_doctor, true);
        }
        menu.setGroupVisible(R.id.group_common, true);
        menu.setGroupVisible(R.id.group_settings, true);

        // Cerrar sesión y navegación segura
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();
                prefs.edit().clear().apply();

                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            }

            // ✅ Usar el controlador de navegación real
            NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawers();
            }
            return handled;
        });

        new FirebaseSyncRepository(this).syncFromFirestore();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        try {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
