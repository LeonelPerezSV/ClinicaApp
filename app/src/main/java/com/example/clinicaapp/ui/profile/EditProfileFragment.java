package com.example.clinicaapp.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ShapeableImageView profileImageView;
    private EditText nameEditText;
    private Button saveButton;
    private AppDatabase db;
    private FirebaseFirestore firestore;

    private String newProfileImageBase64 = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    processAndSetImage(selectedImageUri);
                }
            }
        }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        nameEditText = view.findViewById(R.id.nameEditText);
        saveButton = view.findViewById(R.id.saveButton);

        db = AppDatabase.getInstance(requireContext());
        firestore = FirebaseFirestore.getInstance();

        loadUserData();

        profileImageView.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String currentName = prefs.getString("user_name", "");
        String photoData = prefs.getString("user_photo_url", null);
        nameEditText.setText(currentName);

        if (photoData != null && !photoData.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(photoData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(this).load(decodedByte).into(profileImageView);
            } catch (Exception e) {
                Glide.with(this).load(photoData).error(R.drawable.ic_user_avatar).into(profileImageView);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void processAndSetImage(Uri imageUri) {
        try {
            InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
            Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);
            Bitmap resizedBitmap = getResizedBitmap(selectedBitmap, 400);
            profileImageView.setImageBitmap(resizedBitmap);
            newProfileImageBase64 = bitmapToBase64(resizedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        String newName = nameEditText.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) return;

        updateUserData(userId, newName, newProfileImageBase64);
    }


     //Esto crea el documento si no existe o lo actualiza si ya existe
    private void updateUserData(long userId, String newName, @Nullable String newPhotoBase64) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        if (newPhotoBase64 != null) {
            updates.put("photoUrl", newPhotoBase64);
        }

        firestore.collection("users").document(String.valueOf(userId))
            .set(updates, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                new Thread(() -> {
                    User user = db.userDao().findById((int) userId);
                    if (user != null) {
                        user.setFullName(newName);
                        if (newPhotoBase64 != null) {
                            user.setPhotoUrl(newPhotoBase64);
                        }
                        db.userDao().update(user);
                    }
                }).start();

                SharedPreferences.Editor editor = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE).edit();
                editor.putString("user_name", newName);
                if (newPhotoBase64 != null) {
                    editor.putString("user_photo_url", newPhotoBase64);
                }
                editor.apply();

                Toast.makeText(getContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            })
            .addOnFailureListener(e -> {
                Log.e("EditProfileFragment", "Error al escribir en Firestore", e);
                Toast.makeText(getContext(), "Error al actualizar el perfil en la nube.", Toast.LENGTH_SHORT).show();
            });
    }

    //Comprime la imagen a JPEG en lugar de PNG
    //es más eficiente para fotos y genera cadenas Base64 más cortas.
    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}