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

    public UserRepository(Context context) {
        dao = AppDatabase.getInstance(context).userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<User>> getAll() {
        return dao.getAll();
    }

    public void insert(User user) {
        executor.execute(() -> dao.insert(user));
    }


    public void update(User user) {
        executor.execute(() -> dao.update(user));
    }

    public void delete(User user) {
        executor.execute(() -> dao.delete(user));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public User findById(int id) {
        return dao.findById(id);
    }

    public User login(String username, String password) {
        return dao.login(username, password);
    }


    public void deleteById(int id) {

    }

    public LiveData<User> getById(int id) {
        return dao.getById(id);
    }
}
