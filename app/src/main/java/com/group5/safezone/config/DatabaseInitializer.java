package com.group5.safezone.config;

import com.group5.safezone.model.entity.User;
import com.group5.safezone.config.PasswordUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void populateAsync(AppDatabase db) {
        executor.execute(() -> {
            populateWithTestData(db);
        });
    }

    private static void populateWithTestData(AppDatabase db) {
        // Xóa tất cả dữ liệu cũ
        db.userDao().deleteAllUsers();

        // Tạo danh sách users mẫu
        List<User> sampleUsers = createSampleUsers();

        // Insert dữ liệu vào database
        db.userDao().insertMultipleUsers(sampleUsers);
    }

    private static List<User> createSampleUsers() {
        List<User> users = new ArrayList<>();

        // Admin user
        User admin = new User();
        admin.setUserName("admin");
        admin.setPassword(PasswordUtils.hashPassword("admin123")); // Hash password
        admin.setEmail("admin@safezone.com");
        admin.setPhone("0901234567");
        admin.setGender(true);
        admin.setDob(new Date(90, 0, 1)); // 1990-01-01
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        admin.setBalance(1000000.0);
        admin.setIsVerify(true);
        users.add(admin);

        // Test user
        User testUser = new User();
        testUser.setUserName("testuser");
        testUser.setPassword(PasswordUtils.hashPassword("test123")); // Hash password
        testUser.setEmail("test@example.com");
        testUser.setPhone("0945678901");
        testUser.setGender(true);
        testUser.setDob(new Date(100, 8, 5)); // 2000-09-05
        testUser.setRole("USER");
        testUser.setStatus("ACTIVE"); // Đổi thành ACTIVE để có thể login
        testUser.setBalance(50000.0);
        testUser.setIsVerify(false);
        users.add(testUser);

        return users;
    }
}
