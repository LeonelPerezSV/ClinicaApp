package com.example.clinicaapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "prescriptions")
public class Prescription {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int patientId;
    private String date;
    private String medication;
    private String dosage;
    private String notes;

    public Prescription() {}

    @Ignore
    public Prescription(int patientId, String date, String medication, String dosage, String notes) {
        this.patientId = patientId;
        this.date = date;
        this.medication = medication;
        this.dosage = dosage;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
