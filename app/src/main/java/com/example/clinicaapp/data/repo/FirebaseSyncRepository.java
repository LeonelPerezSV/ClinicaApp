package com.example.clinicaapp.data.repo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSyncRepository {

    private final FirebaseFirestore fs;
    private final AppDatabase db;
    private final Context context;
    private static final String TAG = "FirebaseSync";

    public FirebaseSyncRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
        try {
            FirebaseApp.initializeApp(context);
        } catch (Exception ignored) {}
        fs = FirebaseFirestore.getInstance();
        db = AppDatabase.getInstance(context);
    }

    // =============================================================
    // 🔸 USERS
    // =============================================================
    public void upsertUser(User u) {
        if (u == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("fullName", u.getFullName());
        data.put("username", u.getUsername());
        data.put("userType", u.getUserType());
        fs.collection("users").document("user_" + u.getId()).set(data)
                .addOnSuccessListener(v -> Log.d(TAG, "✅ User synced: " + u.getUsername()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ User sync failed", e));
    }

    // Compatibilidad con RegisterActivity
    public void syncUserToFirestore(@NonNull User user) {
        upsertUser(user);
        Toast.makeText(context, "Usuario sincronizado en Firestore", Toast.LENGTH_SHORT).show();
    }

    // Compatibilidad con MainActivity
    public void syncFromFirestore() {
        fs.collection("users").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    User u = new User(
                            d.getString("fullName"),
                            d.getString("username"),
                            "",
                            d.getString("userType")
                    );
                    u.setId(d.getLong("id").intValue());
                    db.userDao().insert(u);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping user", e);
                }
            }
            Log.d(TAG, "⬇️ Users synced locally");
        }).addOnFailureListener(e -> Log.e(TAG, "❌ Error pulling users", e));
    }

    // =============================================================
    // 🔸 PATIENTS
    // =============================================================
    public void upsertPatient(Patient p) {
        if (p == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("firstName", p.getFirstName());
        data.put("lastName", p.getLastName());
        data.put("email", p.getEmail());
        data.put("phone", p.getPhone());
        data.put("userId", p.getUserId());
        data.put("createdAt", p.getCreatedAt());
        fs.collection("patients").document("patient_" + p.getId()).set(data)
                .addOnSuccessListener(v -> Log.d(TAG, "✅ Patient synced: " + p.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Patient sync failed", e));
    }

    public void deletePatient(int id) {
        fs.collection("patients").document("patient_" + id).delete();
    }

    public void pullPatientsDown() {
        fs.collection("patients").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    Patient p = new Patient();
                    p.setId(d.getLong("id").intValue());
                    p.setFirstName(d.getString("firstName"));
                    p.setLastName(d.getString("lastName"));
                    p.setEmail(d.getString("email"));
                    p.setPhone(d.getString("phone"));
                    p.setUserId(d.getLong("userId").intValue());
                    db.patientDao().insert(p);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping patient", e);
                }
            }
            Log.d(TAG, "⬇️ Patients synced locally");
        });
    }

    // =============================================================
    // 🔸 APPOINTMENTS
    // =============================================================
    public void upsertAppointment(Appointment a) {
        if (a == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", a.getId());
        data.put("doctorId", a.getDoctorId());
        data.put("patientId", a.getPatientId());
        data.put("date", a.getDate());
        data.put("time", a.getTime());
        data.put("status", a.getStatus());
        data.put("reason", a.getReason());
        fs.collection("appointments").document("appointment_" + a.getId()).set(data)
                .addOnSuccessListener(v -> Log.d(TAG, "✅ Appointment synced: " + a.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Appointment sync failed", e));
    }

    public void deleteAppointment(int id) {
        fs.collection("appointments").document("appointment_" + id).delete();
    }

    public void pullAppointmentsDown() {
        fs.collection("appointments").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    Appointment a = new Appointment();
                    a.setId(d.getLong("id").intValue());
                    a.setDoctorId(d.getLong("doctorId").intValue());
                    a.setPatientId(d.getLong("patientId").intValue());
                    a.setDate(d.getString("date"));
                    a.setTime(d.getString("time"));
                    a.setStatus(d.getString("status"));
                    a.setReason(d.getString("reason"));
                    db.appointmentDao().insert(a);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping appointment", e);
                }
            }
            Log.d(TAG, "⬇️ Appointments synced locally");
        });
    }

    // =============================================================
    // 🔸 MEDICAL RECORDS
    // =============================================================
    public void upsertRecord(MedicalRecord r) {
        if (r == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", r.getId());
        data.put("patientId", r.getPatientId());
        data.put("summary", r.getSummary());
        data.put("allergies", r.getAllergies());
        data.put("notes", r.getNotes());
        fs.collection("medical_records").document("record_" + r.getId()).set(data)
                .addOnSuccessListener(v -> Log.d(TAG, "✅ Record synced: " + r.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Record sync failed", e));
    }

    public void deleteRecord(int id) {
        fs.collection("medical_records").document("record_" + id).delete();
    }

    public void pullRecordsDown() {
        fs.collection("medical_records").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    MedicalRecord r = new MedicalRecord();
                    r.setId(d.getLong("id").intValue());
                    r.setPatientId(d.getLong("patientId").intValue());
                    r.setSummary(d.getString("summary"));
                    r.setAllergies(d.getString("allergies"));
                    r.setNotes(d.getString("notes"));
                    db.medicalRecordDao().insert(r);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping record", e);
                }
            }
            Log.d(TAG, "⬇️ Records synced locally");
        });
    }

    // =============================================================
    // 🔸 PRESCRIPTIONS
    // =============================================================
    public void upsertPrescription(Prescription p) {
        if (p == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("patientId", p.getPatientId());
        data.put("date", p.getDate());
        data.put("medication", p.getMedication());
        data.put("dosage", p.getDosage());
        data.put("notes", p.getNotes());
        fs.collection("prescriptions").document("prescription_" + p.getId()).set(data)
                .addOnSuccessListener(v -> Log.d(TAG, "✅ Prescription synced: " + p.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Prescription sync failed", e));
    }

    public void deletePrescription(int id) {
        fs.collection("prescriptions").document("prescription_" + id).delete();
    }

    public void pullPrescriptionsDown() {
        fs.collection("prescriptions").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    Prescription p = new Prescription();
                    p.setId(d.getLong("id").intValue());
                    p.setPatientId(d.getLong("patientId").intValue());
                    p.setDate(d.getString("date"));
                    p.setMedication(d.getString("medication"));
                    p.setDosage(d.getString("dosage"));
                    p.setNotes(d.getString("notes"));
                    db.prescriptionDao().insert(p);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping prescription", e);
                }
            }
            Log.d(TAG, "⬇️ Prescriptions synced locally");
        });
    }

    // =============================================================
    // 🔸 FULL SYNC (usado por LoginActivity)
    // =============================================================
    public void pullAllDown() {
        syncFromFirestore(); // usuarios
        pullPatientsDown();
        pullAppointmentsDown();
        pullRecordsDown();
        pullPrescriptionsDown();
        Log.d(TAG, "⬇️ All entities downloaded from Firestore");
    }
}
