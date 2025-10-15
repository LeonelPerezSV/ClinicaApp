package com.example.clinicaapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Random;

@Entity(tableName = "doctors")
public class Doctor {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String specialty;
    private String email;
    private String phone;

    // Constructor vacío
    public Doctor() {}

    // Constructor que genera un ID aleatorio si no lo tiene
    @Ignore
    public Doctor(String name, String specialty, String email, String phone) {
        this.id = new Random().nextInt(100000); // ID aleatorio
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.phone = phone;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
