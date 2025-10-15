package com.example.clinicaapp.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "doctor")
public class Doctor {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String firstName;
    private String lastName;
    private String specialty;
    private String email;

    public Doctor() {
        // Constructor vacío requerido por Room y fragments
    }

    @Ignore
    public Doctor(String firstName, String lastName, String specialty, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.email = email;
    }


    // ===== GETTERS =====
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialty() { return specialty; }
    public String getEmail() { return email; }

    // ===== SETTERS =====
    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setEmail(String email) { this.email = email; }
}
