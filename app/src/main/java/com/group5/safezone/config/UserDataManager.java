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
