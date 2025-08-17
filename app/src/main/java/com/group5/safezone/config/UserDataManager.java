package com.group5.safezone.config;

import android.app.Application;

import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDataManager {
    private UserRepository userRepository;

    public UserDataManager(Application application) {
        this.userRepository = new UserRepository(application);
    }

    public void initializeSampleData() {
        List<User> sampleUsers = createSampleUsers();
        userRepository.insertMultipleUsers(sampleUsers);
    }

    private List<User> createSampleUsers() {
        List<User> users = new ArrayList<>();

        // Admin user
        User admin = createUser(
                "admin",
                "admin123",
                "admin@safezone.com",
                "0901234567",
                true,
                new Date(90, 0, 1),
                "ADMIN",
                "ACTIVE",
                1000000.0,
                true
        );
        users.add(admin);

        // Test user
        User testUser = createUser(
                "testuser",
                "test123",
                "test@example.com",
                "0945678901",
                true,
                new Date(100, 8, 5),
                "USER",
                "PENDING",
                50000.0,
                false
        );
        users.add(testUser);

        return users;
    }

    private User createUser(String userName, String password, String email, String phone,
                            Boolean gender, Date dob, String role, String status,
                            Double balance, Boolean isVerify) {
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setGender(gender);
        user.setDob(dob);
        user.setRole(role);
        user.setStatus(status);
        user.setBalance(balance);
        user.setIsVerify(isVerify);
        return user;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
