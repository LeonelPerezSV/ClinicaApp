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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ShapeableImageView profileImageView;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText specialtyEditText;
    private TextInputLayout specialtyInputLayout;
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
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        specialtyEditText = view.findViewById(R.id.specialtyEditText);
        specialtyInputLayout = view.findViewById(R.id.specialtyInputLayout);
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
        String email = prefs.getString("user_email", "");
        String photoData = prefs.getString("user_photo_url", null);
        String userType = prefs.getString("user_type", "Paciente");
        String phone = prefs.getString("user_phone", "");
        String specialty = prefs.getString("user_specialty", "");

        nameEditText.setText(currentName);
        emailEditText.setText(email);
        phoneEditText.setText(phone);

        if ("Doctor".equals(userType)) {
            specialtyInputLayout.setVisibility(View.VISIBLE);
            specialtyEditText.setText(specialty);
        } else {
            specialtyInputLayout.setVisibility(View.GONE);
        }

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
        String newPhone = phoneEditText.getText().toString().trim();
        String newSpecialty = specialtyEditText.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) return;

        updateUserData(userId, newName, newPhone, newSpecialty, newProfileImageBase64);
    }

    private void updateUserData(long userId, String newName, String newPhone, String newSpecialty, @Nullable String newPhotoBase64) {
        // Update local UI and DB immediately for better UX
        updateLocalData(userId, newName, newPhone, newSpecialty, newPhotoBase64);

        // --- Update Firestore --- //

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("fullName", newName);
        if (newPhotoBase64 != null) {
            userUpdates.put("photoUrl", newPhotoBase64);
        }
        firestore.collection("users").document(String.valueOf(userId))
            .set(userUpdates, SetOptions.merge())
            .addOnSuccessListener(aVoid -> Log.d("EditProfileFragment", "User main data updated in Firestore."))
            .addOnFailureListener(e -> Log.e("EditProfileFragment", "Error updating user main data", e));

        Map<String, Object> subCollectionUpdates = new HashMap<>();
        subCollectionUpdates.put("phone", newPhone);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        String userType = prefs.getString("user_type", "");

        if ("Doctor".equals(userType)) {
            subCollectionUpdates.put("specialty", newSpecialty);
            firestore.collection("doctors").document(String.valueOf(userId))
                .set(subCollectionUpdates, SetOptions.merge());
        } else if ("Paciente".equals(userType)) {
            firestore.collection("patients").whereEqualTo("userId", userId).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String patientDocId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        firestore.collection("patients").document(patientDocId)
                            .set(subCollectionUpdates, SetOptions.merge());
                    }
                });
        }

        Toast.makeText(getContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void updateLocalData(long userId, String newName, String newPhone, String newSpecialty, @Nullable String newPhotoBase64) {
        // Update SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("ClinicaAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", newName);
        editor.putString("user_phone", newPhone);
        if ("Doctor".equals(prefs.getString("user_type", ""))) {
            editor.putString("user_specialty", newSpecialty);
        }
        if (newPhotoBase64 != null) {
            editor.putString("user_photo_url", newPhotoBase64);
        }
        editor.apply();

        // Update local database in background
        new Thread(() -> {
            User user = db.userDao().findById((int) userId);
            if (user != null) {
                user.setFullName(newName);
                user.setPhone(newPhone);
                if ("Doctor".equals(user.getUserType())) {
                    user.setSpecialty(newSpecialty);
                }
                if (newPhotoBase64 != null) {
                    user.setPhotoUrl(newPhotoBase64);
                }
                db.userDao().update(user);
            }
        }).start();
    }

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
