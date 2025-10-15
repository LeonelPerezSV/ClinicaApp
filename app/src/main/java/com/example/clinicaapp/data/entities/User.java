package com.example.clinicaapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fullName;
    private String username;
    private String password;
    private String userType; // Paciente o Doctor
    private String role;     // admin, doctor, patient (si lo usas luego)

    // --- Constructor por defecto requerido por Room ---
    public User() {
    }

    // --- Constructor completo (para registrar usuarios manualmente) ---
    @Ignore
    public User(String fullName, String username, String password, String userType) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
