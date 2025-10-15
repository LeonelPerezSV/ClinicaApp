package com.example.clinicaapp.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int doctorId;
    private int patientId;
    private String date;
    private String time;
    private String status; // pendiente, completada, cancelada
    private String reason; // ✅ nuevo campo

    public Appointment() {}


    @Ignore // ✅ evita ambigüedad
    public Appointment(int doctorId, int patientId, String date, String time, String status, String reason) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reason = reason;
    }



    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDateTime() {
        return date + " " + time;
    }

    @Ignore
    public String getPatientName() {
        // Este método puede obtener el nombre del paciente si ya tienes PatientDao
        return "Paciente #" + patientId; // Placeholder
    }

    @Ignore
    public String getDoctorName() {
        // Este método puede obtener el nombre del doctor si ya tienes DoctorDao
        return "Doctor #" + doctorId; // Placeholder
    }

}
