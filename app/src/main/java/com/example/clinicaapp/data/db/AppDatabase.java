package com.example.clinicaapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.clinicaapp.data.dao.AppointmentDao;
import com.example.clinicaapp.data.dao.DoctorDao;
import com.example.clinicaapp.data.dao.MedicalRecordDao;
import com.example.clinicaapp.data.dao.PatientDao;
import com.example.clinicaapp.data.dao.PrescriptionDao;
import com.example.clinicaapp.data.dao.UserDao;

import com.example.clinicaapp.data.entities.Appointment;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.data.entities.Patient;
import com.example.clinicaapp.data.entities.Prescription;
import com.example.clinicaapp.data.entities.User;


@Database(
        entities = {
                User.class,
                Doctor.class,
                Patient.class,
                Appointment.class,
                Prescription.class,
                MedicalRecord.class
        },
        version = 2,
        exportSchema = false
)


public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    // 🔹 DAOs válidos
    public abstract UserDao userDao();
    public abstract DoctorDao doctorDao();
    public abstract PatientDao patientDao();
    public abstract AppointmentDao appointmentDao();
    public abstract PrescriptionDao prescriptionDao();
    public abstract MedicalRecordDao medicalRecordDao();

    // 🔹 Singleton para acceso global
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "clinica_db"
                    )
                    .allowMainThreadQueries() // solo durante desarrollo
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
