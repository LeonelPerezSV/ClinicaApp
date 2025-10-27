package com.example.clinicaapp.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.dao.UserDao;
import com.example.clinicaapp.data.db.AppDatabase;
import com.example.clinicaapp.data.entities.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao dao;
    private final ExecutorService executor;
    private final FirebaseSyncRepository sync;

    public UserRepository(Context context) {
        dao = AppDatabase.getInstance(context).userDao();
        executor = Executors.newSingleThreadExecutor();
        sync = new FirebaseSyncRepository(context);
    }

    // 🔹 Obtener todos los usuarios
    public LiveData<List<User>> getAll() {
        return dao.getAll();
    }

    // 🔹 Insertar usuario y sincronizar
    public void insert(User user) {
        executor.execute(() -> {
            dao.insert(user);
            sync.upsertUser(user);
        });
    }

    // 🔹 Actualizar usuario
    public void update(User user) {
        executor.execute(() -> {
            dao.update(user);
            sync.upsertUser(user);
        });
    }

    // 🔹 Eliminar usuario completo
    public void delete(User user) {
        executor.execute(() -> {
            dao.delete(user);
        });
    }

    // 🔹 Eliminar por ID
    public void deleteById(int id) {
        executor.execute(() -> {
            dao.deleteById(id);
        });
    }

    // 🔹 Eliminar todos los usuarios
    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    // 🔹 Login (sin Firebase)
    public User login(String username, String password) {
        return dao.login(username, password);
    }

    // 🔹 Obtener usuario por ID
    public LiveData<User> getById(int id) {
        return dao.getById(id);
    }

    // 🔹 Sincronizar todos los usuarios desde Firestore
    public void syncAll() {
        executor.execute(sync::syncFromFirestore);
    }
}
