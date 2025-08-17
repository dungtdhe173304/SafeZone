package com.group5.safezone.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.UserRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private ExecutorService executor;
    private MutableLiveData<List<User>> allUsers;
    private MutableLiveData<User> currentUser;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        executor = Executors.newFixedThreadPool(4);
        allUsers = new MutableLiveData<>();
        currentUser = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadAllUsers() {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                List<User> users = repository.getAllUsers();
                allUsers.postValue(users);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading users: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void getUserById(int userId) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                User user = repository.getUserById(userId);
                currentUser.postValue(user);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading user: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void getUserByEmail(String email) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                User user = repository.getUserByEmail(email);
                currentUser.postValue(user);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading user: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void getUserByUserName(String userName) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                User user = repository.getUserByUserName(userName);
                currentUser.postValue(user);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading user: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void insertUser(User user) {
        executor.execute(() -> {
            try {
                repository.insertUser(user);
                loadAllUsers(); // Refresh danh sách
            } catch (Exception e) {
                errorMessage.postValue("Error inserting user: " + e.getMessage());
            }
        });
    }

    public void updateUser(User user) {
        executor.execute(() -> {
            try {
                repository.updateUser(user);
                loadAllUsers(); // Refresh danh sách
            } catch (Exception e) {
                errorMessage.postValue("Error updating user: " + e.getMessage());
            }
        });
    }

    public void deleteUser(User user) {
        executor.execute(() -> {
            try {
                repository.deleteUser(user);
                loadAllUsers(); // Refresh danh sách
            } catch (Exception e) {
                errorMessage.postValue("Error deleting user: " + e.getMessage());
            }
        });
    }

    public void softDeleteUser(int userId) {
        executor.execute(() -> {
            try {
                repository.softDeleteUser(userId);
                loadAllUsers(); // Refresh danh sách
            } catch (Exception e) {
                errorMessage.postValue("Error soft deleting user: " + e.getMessage());
            }
        });
    }

    public void updateBalance(int userId, double amount) {
        executor.execute(() -> {
            try {
                repository.updateBalance(userId, amount);
                getUserById(userId); // Refresh user hiện tại
            } catch (Exception e) {
                errorMessage.postValue("Error updating balance: " + e.getMessage());
            }
        });
    }

    public void getUsersByRole(String role) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                List<User> users = repository.getUsersByRole(role);
                allUsers.postValue(users);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading users by role: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
