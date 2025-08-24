package com.group5.safezone.repository;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.ProductImagesDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.dao.TransactionsDao;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.entity.Transactions;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.os.Looper;

public class ProductRepository {
    private ProductDao productDao;
    private ProductImagesDao productImagesDao;
    private UserDao userDao;
    private TransactionsDao transactionsDao;
    private ExecutorService executor;
    private Handler mainHandler;

    public ProductRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.productDao = db.productDao();
        this.productImagesDao = db.productImagesDao();
        this.userDao = db.userDao();
        this.transactionsDao = db.transactionsDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface OnProductOperationListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnProductImagesLoadedListener {
        void onImagesLoaded(List<ProductImages> images);
        void onError(String error);
    }

    public interface OnProductDetailsLoadedListener {
        void onProductDetailsLoaded(Product product, List<ProductImages> images);
        void onError(String error);
    }
    
    public interface OnProductSoldStatusListener {
        void onProductSoldStatusChecked(boolean sold);
        void onError(String error);
    }

    public interface OnProductsLoadedListener {
        void onProductsLoaded(List<Product> products, List<List<ProductImages>> productImagesList, Map<Integer, String> userNames);
        void onError(String error);
    }

    public void increaseProductViews(String productId, OnProductOperationListener listener) {
        executor.execute(() -> {
            try {
                productDao.incrementView(productId);
                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess());
                }
            } catch (Exception e) {
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to update views: " + e.getMessage()));
                }
            }
        });
    }

    public interface OnBalanceCheckListener {
        void onSufficientBalance();
        void onInsufficientBalance(double currentBalance, double requiredAmount);
        void onError(String error);
    }

    public void checkBalanceForPosting(int userId, OnBalanceCheckListener listener) {
        executor.execute(() -> {
            try {
                User user = userDao.getUserById(userId);
                if (user == null) {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onError("User not found"));
                    }
                    return;
                }
                
                double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
                double postingFee = 5000.0; // Phí đăng bài 5.000
                
                if (currentBalance >= postingFee) {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onSufficientBalance());
                    }
                } else {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onInsufficientBalance(currentBalance, postingFee));
                    }
                }
            } catch (Exception e) {
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Error checking balance: " + e.getMessage()));
                }
            }
        });
    }

    public void deductPostingFeeAndCreateTransaction(int userId, String productId, OnProductOperationListener listener) {
        executor.execute(() -> {
            try {
                // Trừ phí từ balance
                double postingFee = 5000.0;
                userDao.updateBalance(userId, -postingFee);
                
                // Tạo transaction record
                Transactions transaction = new Transactions();
                transaction.setUserId(userId);
                transaction.setTransactionType("POSTING_FEE");
                transaction.setAmount(-postingFee); // Số âm vì là trừ tiền
                    transaction.setDescription("Phí đăng bài sản phẩm: " + productId);
                transaction.setStatus("SUCCESS");
                transaction.setReferenceId(productId);
                
                transactionsDao.insert(transaction);
                
                // Log để debug
                android.util.Log.d("ProductRepository", "Posting fee deducted: " + postingFee + " for user: " + userId);
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess());
                }
            } catch (Exception e) {
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to process posting fee: " + e.getMessage()));
                }
            }
        });
    }

    public void insertProduct(Product product, List<String> imagePaths, OnProductOperationListener listener) {
        System.out.println("=== ProductRepository: Starting insertProduct ===");
        System.out.println("=== Product ID: " + product.getId() + " ===");
        System.out.println("=== User ID: " + product.getUserId() + " ===");
        System.out.println("=== Product Name: " + product.getProductName() + " ===");
        System.out.println("=== Image Paths Count: " + (imagePaths != null ? imagePaths.size() : 0) + " ===");
        
        executor.execute(() -> {
            try {
                System.out.println("=== ProductRepository: Executing on background thread ===");
                
                // Validate product data
                if (product.getId() == null || product.getId().trim().isEmpty()) {
                    System.out.println("=== ProductRepository: Validation failed - Product ID is null/empty ===");
                    if (listener != null) {
                        mainHandler.post(() -> listener.onError("Product ID is required"));
                    }
                    return;
                }
                if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
                    System.out.println("=== ProductRepository: Validation failed - Product name is null/empty ===");
                    if (listener != null) {
                        mainHandler.post(() -> listener.onError("Product name is required"));
                    }
                    return;
                }
                if (product.getUserId() <= 0) {
                    System.out.println("=== ProductRepository: Validation failed - User ID is invalid: " + product.getUserId() + " ===");
                    if (listener != null) {
                        mainHandler.post(() -> listener.onError("Valid user ID is required"));
                    }
                    return;
                }
                
                System.out.println("=== ProductRepository: Validation passed, inserting product ===");
                
                // Insert product
                productDao.insert(product);
                System.out.println("=== ProductRepository: Product inserted successfully ===");
                
                // Insert product images
                if (imagePaths != null && !imagePaths.isEmpty()) {
                    System.out.println("=== ProductRepository: Inserting " + imagePaths.size() + " images ===");
                    List<ProductImages> productImages = new ArrayList<>();
                    for (String imagePath : imagePaths) {
                        ProductImages image = new ProductImages();
                        image.setProductId(product.getId());
                        image.setPath(imagePath);
                        image.setName("product_image");
                        productImages.add(image);
                    }
                    productImagesDao.insertMultiple(productImages);
                    System.out.println("=== ProductRepository: Images inserted successfully ===");
                } else {
                    System.out.println("=== ProductRepository: No images to insert ===");
                }
                
                System.out.println("=== ProductRepository: All operations completed successfully ===");
                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess());
                }
            } catch (Exception e) {
                System.out.println("=== ProductRepository: Error occurred: " + e.getMessage() + " ===");
                e.printStackTrace();
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Database error: " + e.getMessage()));
                }
            }
        });
    }

    public void getAllProducts(OnProductsLoadedListener listener) {
        executor.execute(() -> {
            try {
                List<Product> products = productDao.getAllProducts();
                if (products != null && !products.isEmpty()) {
                    // Load images and user names for each product
                    loadProductDetails(products, listener);
                } else {
                    mainHandler.post(() -> listener.onProductsLoaded(new ArrayList<>(), new ArrayList<>(), new HashMap<>()));
                }
            } catch (Exception e) {
                mainHandler.post(() -> listener.onError("Error loading products: " + e.getMessage()));
            }
        });
    }

    public void getProductsForCurrentUser(int currentUserId, OnProductsLoadedListener listener) {
        executor.execute(() -> {
            try {
                List<Product> allProducts = productDao.getAllProducts();
                List<Product> visibleProducts = new ArrayList<>();
                
                for (Product product : allProducts) {
                    if (isProductVisibleToUser(product, currentUserId)) {
                        visibleProducts.add(product);
                    }
                }
                
                if (visibleProducts != null && !visibleProducts.isEmpty()) {
                    // Load images and user names for each product
                    loadProductDetails(visibleProducts, listener);
                } else {
                    mainHandler.post(() -> listener.onProductsLoaded(new ArrayList<>(), new ArrayList<>(), new HashMap<>()));
                }
            } catch (Exception e) {
                mainHandler.post(() -> listener.onError("Error loading products for user: " + e.getMessage()));
            }
        });
    }

    private boolean isProductVisibleToUser(Product product, int currentUserId) {
        System.out.println("=== ProductRepository: Checking visibility for product: " + product.getProductName() + " ===");
        System.out.println("=== Product ID: " + product.getId() + " ===");
        System.out.println("=== Product Owner ID: " + product.getUserId() + " ===");
        System.out.println("=== Current User ID: " + currentUserId + " ===");
        System.out.println("=== Public/Private: " + product.getPublicPrivate() + " ===");
        System.out.println("=== UserView: " + product.getUserView() + " ===");
        
        // Public products are visible to everyone
        if ("public".equals(product.getPublicPrivate())) {
            System.out.println("=== Product is PUBLIC - visible to everyone ===");
            return true;
        }
        
        // Private products: only visible to owner or users in userView
        if ("private".equals(product.getPublicPrivate())) {
            System.out.println("=== Product is PRIVATE - checking access ===");
            
            // Owner can always see their own products
            if (product.getUserId() == currentUserId) {
                System.out.println("=== Current user is OWNER - visible ===");
                return true;
            }
            
            // Check if current user is in userView list (username-based)
            String userView = product.getUserView();
            if (userView != null && !userView.trim().isEmpty()) {
                System.out.println("=== UserView not empty: " + userView + " ===");
                
                // Get current user's username to compare
                User currentUser = userDao.getUserById(currentUserId);
                if (currentUser != null && currentUser.getUserName() != null) {
                    String currentUsername = currentUser.getUserName().trim();
                    System.out.println("=== Current username: " + currentUsername + " ===");
                    
                    // Split userView by comma and check each username
                    String[] allowedUsernames = userView.split(",");
                    System.out.println("=== Allowed Usernames: " + Arrays.toString(allowedUsernames) + " ===");
                    
                    for (String allowedUsername : allowedUsernames) {
                        String trimmedUsername = allowedUsername.trim();
                        System.out.println("=== Checking Username: '" + trimmedUsername + "' vs Current: '" + currentUsername + "' ===");
                        
                        if (trimmedUsername.equalsIgnoreCase(currentUsername)) {
                            System.out.println("=== Current user found in userView - visible ===");
                            return true;
                        }
                    }
                    System.out.println("=== Current user NOT in userView - NOT visible ===");
                } else {
                    System.out.println("=== Cannot get current user info - NOT visible ===");
                }
            } else {
                System.out.println("=== UserView is empty - only owner can see ===");
            }
            
            // If userView is empty, only owner can see
            return false;
        }
        
        // Default: visible to everyone
        System.out.println("=== Default case - visible to everyone ===");
        return true;
    }

    public void getProductsByUserId(int userId, OnProductsLoadedListener listener) {
        executor.execute(() -> {
            try {
                List<Product> products = productDao.getProductsByUserId(userId);
                if (products != null && !products.isEmpty()) {
                    // Load images and user names for each product
                    loadProductDetails(products, listener);
                } else {
                    mainHandler.post(() -> listener.onProductsLoaded(new ArrayList<>(), new ArrayList<>(), new HashMap<>()));
                }
            } catch (Exception e) {
                mainHandler.post(() -> listener.onError("Error loading products by user ID: " + e.getMessage()));
            }
        });
    }

    private void loadProductDetails(List<Product> products, OnProductsLoadedListener listener) {
        executor.execute(() -> {
            try {
                List<List<ProductImages>> allImages = new ArrayList<>();
                Map<Integer, String> userNames = new HashMap<>();
                
                for (Product product : products) {
                    // Load images for this product
                    List<ProductImages> images = productImagesDao.getImagesByProductId(product.getId());
                    allImages.add(images);
                    
                    // Load user name for this product
                    User user = userDao.getUserById(product.getUserId());
                    if (user != null && user.getUserName() != null) {
                        userNames.put(product.getUserId(), user.getUserName());
                    } else {
                        userNames.put(product.getUserId(), "Unknown User");
                    }
                }
                
                mainHandler.post(() -> listener.onProductsLoaded(products, allImages, userNames));
            } catch (Exception e) {
                mainHandler.post(() -> listener.onError("Error loading product details: " + e.getMessage()));
            }
        });
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void updateProduct(Product product, List<String> newImagePaths, OnProductOperationListener listener) {
        System.out.println("=== ProductRepository: Starting updateProduct ===");
        System.out.println("=== Product ID: " + product.getId() + " ===");
        System.out.println("=== User ID: " + product.getUserId() + " ===");
        System.out.println("=== New Image Paths Count: " + (newImagePaths != null ? newImagePaths.size() : 0) + " ===");
        
        executor.execute(() -> {
            try {
                System.out.println("=== ProductRepository: Executing updateProduct on background thread ===");
                
                // Update product in database
                productDao.update(product);
                System.out.println("=== ProductRepository: Product updated successfully ===");
                
                // Handle images: delete old ones and insert new ones
                if (newImagePaths != null && !newImagePaths.isEmpty()) {
                    System.out.println("=== ProductRepository: Updating images ===");
                    
                    // Delete old images
                    productImagesDao.deleteImagesByProductId(product.getId());
                    System.out.println("=== ProductRepository: Old images deleted ===");
                    
                    // Insert new images
                    List<ProductImages> productImages = new ArrayList<>();
                    for (String imagePath : newImagePaths) {
                        ProductImages image = new ProductImages();
                        image.setProductId(product.getId());
                        image.setPath(imagePath);
                        image.setName("product_image");
                        productImages.add(image);
                    }
                    productImagesDao.insertMultiple(productImages);
                    System.out.println("=== ProductRepository: New images inserted successfully ===");
                } else {
                    System.out.println("=== ProductRepository: No new images to update ===");
                }
                
                System.out.println("=== ProductRepository: All update operations completed successfully ===");
                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess());
                }
            } catch (Exception e) {
                System.out.println("=== ProductRepository: Error updating product: " + e.getMessage() + " ===");
                e.printStackTrace();
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to update product: " + e.getMessage()));
                }
            }
        });
    }

    public void deleteProduct(String productId, OnProductOperationListener listener) {
        executor.execute(() -> {
            try {
                // Soft delete product
                productDao.softDelete(productId);
                
                // Delete associated images
                productImagesDao.deleteImagesByProductId(productId);
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to delete product: " + e.getMessage()));
                }
            }
        });
    }

    public void loadProductImages(String productId, OnProductImagesLoadedListener listener) {
        System.out.println("=== ProductRepository: Loading images for product: " + productId + " ===");
        
        executor.execute(() -> {
            try {
                System.out.println("=== ProductRepository: Executing loadProductImages on background thread ===");
                
                List<ProductImages> images = productImagesDao.getImagesByProductId(productId);
                System.out.println("=== ProductRepository: Loaded " + (images != null ? images.size() : "0") + " images from database ===");
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onImagesLoaded(images));
                }
            } catch (Exception e) {
                System.out.println("=== ProductRepository: Error loading images: " + e.getMessage() + " ===");
                e.printStackTrace();
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to load images: " + e.getMessage()));
                }
            }
        });
    }

    public void loadProductDetails(String productId, OnProductDetailsLoadedListener listener) {
        System.out.println("=== ProductRepository: Loading product details for ID: " + productId + " ===");
        
        executor.execute(() -> {
            try {
                System.out.println("=== ProductRepository: Executing loadProductDetails on background thread ===");
                
                // Load product from database
                Product product = productDao.getProductById(productId);
                System.out.println("=== ProductRepository: Product loaded: " + (product != null ? product.getProductName() : "NULL") + " ===");
                
                // Load images for this product
                List<ProductImages> images = productImagesDao.getImagesByProductId(productId);
                System.out.println("=== ProductRepository: Images loaded: " + (images != null ? images.size() : 0) + " ===");
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onProductDetailsLoaded(product, images));
                }
            } catch (Exception e) {
                System.out.println("=== ProductRepository: Error loading product details: " + e.getMessage() + " ===");
                e.printStackTrace();
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to load product details: " + e.getMessage()));
                }
            }
        });
    }
    
    public void checkProductSoldStatus(String productId, OnProductSoldStatusListener listener) {
        System.out.println("=== ProductRepository: Checking sold status for product: " + productId + " ===");
        
        executor.execute(() -> {
            try {
                // Check if product has any orders
                // We need to get the database instance from the context
                // Since we don't have direct access to context here, we'll use a different approach
                // We'll need to pass the database instance or use a static method
                com.group5.safezone.model.dao.OrderDao orderDao = AppDatabase.getDatabase(null).orderDao();
                List<com.group5.safezone.model.entity.Order> orders = orderDao.getOrdersByProductId(productId);
                
                boolean isSold = orders != null && !orders.isEmpty();
                System.out.println("=== ProductRepository: Product " + productId + " sold status: " + isSold + " (orders count: " + (orders != null ? orders.size() : 0) + ") ===");
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onProductSoldStatusChecked(isSold));
                }
            } catch (Exception e) {
                System.out.println("=== ProductRepository: Error checking sold status: " + e.getMessage() + " ===");
                e.printStackTrace();
                if (listener != null) {
                    mainHandler.post(() -> listener.onError("Failed to check sold status: " + e.getMessage()));
                }
            }
        });
    }
}
