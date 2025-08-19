package com.group5.safezone.repository;

import android.app.Application;

import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executor;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        executor = Executors.newFixedThreadPool(4);
    }

    // Async methods for UI thread safety
    public Future<List<User>> getAllUsersAsync() {
        return executor.submit(() -> userDao.getAllUsers());
    }

    public Future<User> getUserByIdAsync(int userId) {
        return executor.submit(() -> userDao.getUserById(userId));
    }

    public Future<User> getUserByEmailAsync(String email) {
        return executor.submit(() -> userDao.getUserByEmail(email));
    }

    public Future<User> getUserByUserNameAsync(String userName) {
        return executor.submit(() -> userDao.getUserByUserName(userName));
    }

    public void insertUser(User user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void insertMultipleUsers(List<User> users) {
        executor.execute(() -> userDao.insertMultipleUsers(users));
    }

    public void updateUser(User user) {
        executor.execute(() -> userDao.updateUser(user));
    }

    public void deleteUser(User user) {
        executor.execute(() -> userDao.deleteUser(user));
    }

    public void softDeleteUser(int userId) {
        executor.execute(() -> userDao.softDeleteUser(userId));
    }

    public Future<List<User>> getUsersByRoleAsync(String role) {
        return executor.submit(() -> userDao.getUsersByRole(role));
    }

    public void updateBalance(int userId, double amount) {
        executor.execute(() -> userDao.updateBalance(userId, amount));
    }

    public void deleteAllUsers() {
        executor.execute(() -> userDao.deleteAllUsers());
    }

    // Synchronous methods (chỉ dùng khi cần thiết)
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public User getUserByUserName(String userName) {
        return userDao.getUserByUserName(userName);
    }

    public List<User> getUsersByRole(String role) {
        return userDao.getUsersByRole(role);
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
