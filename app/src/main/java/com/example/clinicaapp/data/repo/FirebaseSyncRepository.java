package com.example.clinicaapp.data.repo;

import android.content.Context;
import android.util.Log;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirebaseSyncRepository {

    private final FirebaseFirestore fs;
    private final AppDatabase db;
    private static final String TAG = "FirebaseSync";

    public FirebaseSyncRepository(Context ctx) {
        Context context = ctx.getApplicationContext();
        try {
            FirebaseApp.initializeApp(context);
        } catch (Exception ignored) {}
        fs = FirebaseFirestore.getInstance();
        db = AppDatabase.getInstance(context);
    }

    // =============================================================
    // - MÉTODOS DE COMPATIBILIDAD
    // =============================================================
    public void syncFromFirestore() {
        pullAllDown();
    }

    public void syncUserToFirestore(User user) {
        upsertUser(user);
    }

    // =============================================================
    // - USERS
    // =============================================================
    public void upsertUser(User u) {
        if (u == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("fullName", u.getFullName());
        data.put("username", u.getUsername());
        data.put("userType", u.getUserType());
        data.put("photoUrl", u.getPhotoUrl());
        fs.collection("users").document(String.valueOf(u.getId())).set(data);
    }

    public void pullUserByEmail(String email, Consumer<User> onUserFetched) {
        fs.collection("users").whereEqualTo("username", email).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        try {
                            User freshUser = new User(userDoc.getString("fullName"), userDoc.getString("username"), "", userDoc.getString("userType"));
                            long id = userDoc.getLong("id") != null ? userDoc.getLong("id") : -1;
                            freshUser.setId((int)id);
                            if (userDoc.contains("photoUrl")) {
                                freshUser.setPhotoUrl(userDoc.getString("photoUrl"));
                            }

                            if ("Doctor".equals(freshUser.getUserType())) {
                                fs.collection("doctors").document(String.valueOf(id)).get()
                                    .addOnSuccessListener(doctorDoc -> {
                                        if (doctorDoc.exists()) {
                                            freshUser.setSpecialty(doctorDoc.getString("specialty"));
                                            freshUser.setPhone(doctorDoc.getString("phone"));
                                        }
                                        saveUserAndCallback(freshUser, onUserFetched);
                                    })
                                    .addOnFailureListener(e -> saveUserAndCallback(freshUser, onUserFetched));
                            } else if ("Paciente".equals(freshUser.getUserType())) {
                                fs.collection("patients").whereEqualTo("userId", id).limit(1).get()
                                    .addOnSuccessListener(patientDocs -> {
                                        if (!patientDocs.isEmpty()) {
                                            DocumentSnapshot patientDoc = patientDocs.getDocuments().get(0);
                                            freshUser.setPhone(patientDoc.getString("phone"));
                                        }
                                        saveUserAndCallback(freshUser, onUserFetched);
                                    })
                                    .addOnFailureListener(e -> saveUserAndCallback(freshUser, onUserFetched));
                            } else {
                                saveUserAndCallback(freshUser, onUserFetched);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error mapping user from Firestore", e);
                            onUserFetched.accept(null);
                        }
                    } else {
                        onUserFetched.accept(null); // User not found
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user by email", e);
                    onUserFetched.accept(null);
                });
    }

    private void saveUserAndCallback(User user, Consumer<User> onUserFetched) {
        new Thread(() -> {
            db.userDao().insert(user);
            if (onUserFetched != null) {
                onUserFetched.accept(user);
            }
        }).start();
    }

    public void pullUsersDown() {
        fs.collection("users").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    User u = new User(d.getString("fullName"), d.getString("username"), "", d.getString("userType"));
                    long id = d.getLong("id").intValue();
                    u.setId((int)id);
                    if (d.contains("photoUrl")) {
                        u.setPhotoUrl(d.getString("photoUrl"));
                    }
                    
                    if ("Doctor".equals(u.getUserType())) {
                        fs.collection("doctors").document(String.valueOf(id)).get()
                            .addOnSuccessListener(doctorDoc -> {
                                if (doctorDoc.exists()) {
                                    u.setSpecialty(doctorDoc.getString("specialty"));
                                    u.setPhone(doctorDoc.getString("phone"));
                                }
                                saveUserAndCallback(u, null);
                            })
                            .addOnFailureListener(e -> saveUserAndCallback(u, null));
                    } else if ("Paciente".equals(u.getUserType())) {
                        fs.collection("patients").whereEqualTo("userId", id).limit(1).get()
                            .addOnSuccessListener(patientDocs -> {
                                if (!patientDocs.isEmpty()) {
                                    DocumentSnapshot patientDoc = patientDocs.getDocuments().get(0);
                                    u.setPhone(patientDoc.getString("phone"));
                                }
                                saveUserAndCallback(u, null);
                            })
                            .addOnFailureListener(e -> saveUserAndCallback(u, null));
                    } else {
                        saveUserAndCallback(u, null);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error mapping user", e);
                }
            }
        });
    }

    // =============================================================
    // - DOCTORS
    // =============================================================
    public void upsertDoctor(Doctor d) {
        if (d == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", d.getId());
        data.put("name", d.getName());
        data.put("specialty", d.getSpecialty());
        data.put("email", d.getEmail());
        data.put("phone", d.getPhone());
        fs.collection("doctors").document(String.valueOf(d.getId())).set(data);
    }

    // =============================================================
    // - PATIENTS
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
        fs.collection("patients").document(String.valueOf(p.getId())).set(data);
    }

    public void deletePatient(int id) {
        fs.collection("patients").document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Patient deleted from Firestore: " + id))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting patient from Firestore: " + id, e));
    }

    public void pullPatientsDown() {
        fs.collection("patients").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    Patient p = new Patient(d.getString("firstName"), d.getString("lastName"), d.getString("email"), d.getString("phone"), d.getLong("userId").intValue());
                    p.setId(d.getLong("id").intValue());
                    db.patientDao().insert(p);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping patient", e);
                }
            }
        });
    }

    // =============================================================
    // - APPOINTMENTS
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
        fs.collection("appointments").document(String.valueOf(a.getId())).set(data);
    }

    public void deleteAppointment(int id) {
        fs.collection("appointments").document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Appointment deleted from Firestore: " + id))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting appointment from Firestore: " + id, e));
    }

    public void pullAppointmentsDown() {
        fs.collection("appointments").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    Appointment a = new Appointment(d.getLong("patientId").intValue(), d.getLong("doctorId").intValue(), d.getString("date"), d.getString("time"), d.getString("reason"), d.getString("status"));
                    a.setId(d.getLong("id").intValue());
                    db.appointmentDao().insert(a);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping appointment", e);
                }
            }
        });
    }

    // =============================================================
    // - MEDICAL RECORDS
    // =============================================================
    public void upsertRecord(MedicalRecord r) {
        if (r == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("id", r.getId());
        data.put("patientId", r.getPatientId());
        data.put("summary", r.getSummary());
        data.put("allergies", r.getAllergies());
        data.put("notes", r.getNotes());
        fs.collection("medical_records").document(String.valueOf(r.getId())).set(data);
    } 

    public void deleteRecord(int id) {
        fs.collection("medical_records").document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Record deleted from Firestore: " + id))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting record from Firestore: " + id, e));
    }

    public void pullRecordsDown() {
        fs.collection("medical_records").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    MedicalRecord r = new MedicalRecord(d.getLong("patientId").intValue(), d.getString("summary"), d.getString("allergies"), d.getString("notes"));
                    r.setId(d.getLong("id").intValue());
                    db.medicalRecordDao().insert(r);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping record", e);
                }
            }
        });
    }

    // =============================================================
    // - PRESCRIPTIONS
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
        fs.collection("prescriptions").document(String.valueOf(p.getId())).set(data);
    }

    public void deletePrescription(int id) {
        fs.collection("prescriptions").document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Prescription deleted from Firestore: " + id))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting prescription from Firestore: " + id, e));
    }

    public void pullPrescriptionsDown() {
        fs.collection("prescriptions").get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot d : snap) {
                try {
                    Prescription p = new Prescription(d.getLong("patientId").intValue(), d.getString("date"), d.getString("medication"), d.getString("dosage"), d.getString("notes"));
                    p.setId(d.getLong("id").intValue());
                    db.prescriptionDao().insert(p);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping prescription", e);
                }
            }
        });
    }

    // =============================================================
    public void pullAllDown() {
        pullUsersDown();
        pullPatientsDown();
        pullAppointmentsDown();
        pullRecordsDown();
        pullPrescriptionsDown();
        Log.d(TAG, "All entities downloaded from Firestore");
    }
}
