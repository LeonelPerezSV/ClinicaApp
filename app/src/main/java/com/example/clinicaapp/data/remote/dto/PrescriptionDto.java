package com.example.clinicaapp.data.remote.dto;

public class PrescriptionDto {
    private int id;
    private int patientId;
    private int doctorId;
    private String date;
    private String medication;
    private String dosage;
    private String notes;

    public PrescriptionDto() {}

    public PrescriptionDto(int id, int patientId, int doctorId, String date, String medication, String dosage, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.medication = medication;
        this.dosage = dosage;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
