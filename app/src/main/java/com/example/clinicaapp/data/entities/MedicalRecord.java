package com.example.clinicaapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
@Entity(tableName = "medical_records")
public class MedicalRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int patientId;
    private String summary;
    private String allergies;
    private String notes;

    public MedicalRecord() {}
    @Ignore
    public MedicalRecord(int patientId, String summary, String allergies, String notes) {
        this.patientId = patientId;
        this.summary = summary;
        this.allergies = allergies;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
