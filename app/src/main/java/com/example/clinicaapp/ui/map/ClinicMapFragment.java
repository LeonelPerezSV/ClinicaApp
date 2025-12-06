package com.example.clinicaapp.ui.map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clinicaapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ClinicMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient locationClient;

    // 🔹 Coordenadas de la clínica
    private final LatLng CLINIC_LOCATION = new LatLng(13.6929, -89.2182); // Ejemplo: San Salvador

    public ClinicMapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clinic_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map, mapFragment)
                    .commit();
        }

        mapFragment.getMapAsync(this);

        FloatingActionButton fabMapType = view.findViewById(R.id.fab_map_type);
        fabMapType.setOnClickListener(v -> showMapTypeDialog());
    }

    private void showMapTypeDialog() {
        final String[] mapTypes = {"Normal", "Satélite", "Híbrido", "Terreno"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar tipo de mapa");
        builder.setItems(mapTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 3:
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // 🔹 Habilitar controles de UI
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        // 🔹 Agregar marcador en la clínica
        map.addMarker(new MarkerOptions()
                .position(CLINIC_LOCATION)
                .title("Clínica Central Agrolife")
                .snippet("Av. Las Gardenias #120, San Salvador"));

        // 🔹 Centrar y aplicar zoom
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(CLINIC_LOCATION, 15));

        enableUserLocation();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permiso de ubicación
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        map.setMyLocationEnabled(true);

        // 🔹 Opcional: mover cámara a ubicación actual
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 14));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        }
    }
}
