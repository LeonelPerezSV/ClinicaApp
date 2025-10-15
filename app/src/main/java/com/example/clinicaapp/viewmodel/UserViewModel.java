package com.example.clinicaapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.clinicaapp.data.entities.User;
import com.example.clinicaapp.data.repo.UserRepository;
import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final LiveData<List<User>> allUsers;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        allUsers = repository.getAll();
    }

    public LiveData<List<User>> getAllUsers() { return allUsers; }
    public void insert(User user) { repository.insert(user); }
    public void update(User user) { repository.update(user); }
    public void delete(User user) { repository.delete(user); }
    public void deleteAll() { repository.deleteAll(); }
    public User login(String username, String password) { return repository.login(username, password); }

    public LiveData<List<User>> getAll() {
        return repository.getAll();
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public LiveData<User> getById(int id) {
        return repository.getById(id);
    }
}
