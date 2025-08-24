package com.group5.safezone.config;

import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.config.PasswordUtils;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.R;

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
        db.productDao().deleteAllProducts();

        // Tạo danh sách users mẫu
        List<User> sampleUsers = createSampleUsers();
 
        // Tạo danh sách products mẫu
        List<Product> sampleProducts = createSampleProducts();

        // Insert dữ liệu vào database
        db.userDao().insertMultipleUsers(sampleUsers);
        db.productDao().insertMultipleProducts(sampleProducts);
        
        // Tạo ảnh mẫu cho products
        createSampleProductImages(db, sampleProducts);
    }
    
    private static void createSampleProductImages(AppDatabase db, List<Product> products) {
        // Tạo ảnh mẫu cho từng product
        for (Product product : products) {
            // Tạo 1-2 ảnh mẫu cho mỗi product
            for (int i = 1; i <= 2; i++) {
                ProductImages image = new ProductImages();
                image.setProductId(product.getId());
                image.setName("sample_image_" + i);
                // Sử dụng ảnh mẫu từ drawable (placeholder)
                image.setPath("android.resource://com.group5.safezone/" + R.drawable.ic_info);
                image.setCreatedAt(new Date());
                image.setUpdatedAt(new Date());
                db.productImagesDao().insert(image);
            }
        }
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

        // Thêm user mới - có thể đăng nhập bằng cả username và email
        User newUser = new User();
        newUser.setUserName("join");
        newUser.setPassword(PasswordUtils.hashPassword("john123")); // Hash password
        newUser.setEmail("john.doe@gmail.com");
        newUser.setPhone("0987654321");
        newUser.setGender(false); // false = nữ
        newUser.setDob(new Date(95, 5, 15)); // 1995-06-15
        newUser.setRole("USER");
        newUser.setStatus("ACTIVE");
        newUser.setBalance(75000.0);
        newUser.setIsVerify(true);
        users.add(newUser);

        return users;
    }

    private static List<Product> createSampleProducts() {
        List<Product> products = new ArrayList<>();

        // Sample product 1
        Product product1 = new Product();
        product1.setId("prod_001");
        product1.setProductName("iPhone 15 Pro");
        product1.setFee(1.0); // Seller pays fee
        product1.setPrice(25000000.0);
        product1.setPublicPrivate("public");
        product1.setStatus("Active");
        product1.setUserId(1);
        product1.setCreatedAt(new Date());
        product1.setUpdatedAt(new Date());
        products.add(product1);

        // Sample product 2
        Product product2 = new Product();
        product2.setId("prod_002");
        product2.setProductName("MacBook Air M2");
        product2.setFee(2.0); // Buyer pays fee
        product2.setPrice(35000000.0);
        product2.setPublicPrivate("private");
        product2.setUserView("admin,testuser");
        product2.setStatus("Active");
        product2.setUserId(2);
        product2.setCreatedAt(new Date());
        product2.setUpdatedAt(new Date());
        products.add(product2);

        // Sample product 3
        Product product3 = new Product();
        product3.setId("prod_003");
        product3.setProductName("AirPods Pro");
        product3.setFee(1.0); // Seller pays fee
        product3.setPrice(8000000.0);
        product3.setPublicPrivate("public");
        product3.setStatus("Active");
        product3.setUserId(1);
        product3.setCreatedAt(new Date());
        product3.setUpdatedAt(new Date());
        products.add(product3);

        return products;
    }
}
