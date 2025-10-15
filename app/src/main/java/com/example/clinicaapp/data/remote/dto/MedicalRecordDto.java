package com.example.clinicaapp.data.remote.dto;

public class MedicalRecordDto {
    private int id;
    private int patientId;
    private String summary;
    private String allergies;
    private String notes;

    public MedicalRecordDto() {}

    public MedicalRecordDto(int id, int patientId, String summary, String allergies, String notes) {
        this.id = id;
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
