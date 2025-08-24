package com.group5.safezone.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.repository.ProductRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductViewModel extends AndroidViewModel {
    private ProductRepository repository;
    private MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private MutableLiveData<List<List<ProductImages>>> productImagesList = new MutableLiveData<>();
    private MutableLiveData<Map<Integer, String>> userNames = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<List<List<ProductImages>>> getProductImagesList() {
        return productImagesList;
    }

    public LiveData<Map<Integer, String>> getUserNames() {
        return userNames;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadAllProducts() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getAllProducts(new ProductRepository.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> productsList, List<List<ProductImages>> imagesList, Map<Integer, String> userNamesMap) {
                products.setValue(productsList);
                productImagesList.setValue(imagesList);
                userNames.setValue(userNamesMap);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void loadProductsForCurrentUser(int currentUserId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getProductsForCurrentUser(currentUserId, new ProductRepository.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> productsList, List<List<ProductImages>> imagesList, Map<Integer, String> userNamesMap) {
                products.setValue(productsList);
                productImagesList.setValue(imagesList);
                userNames.setValue(userNamesMap);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void loadProductsByUserId(int userId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getProductsByUserId(userId, new ProductRepository.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> productsList, List<List<ProductImages>> imagesList, Map<Integer, String> userNamesMap) {
                products.setValue(productsList);
                productImagesList.setValue(imagesList);
                userNames.setValue(userNamesMap);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public interface OnProductAddListener {
        void onSuccess();
        void onInsufficientBalance(double currentBalance, double requiredAmount);
        void onError(String error);
    }

    public interface OnProductUpdateListener {
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

    public void addProduct(Product product, List<String> imagePaths, OnProductAddListener listener) {
        System.out.println("=== ProductViewModel: Starting addProduct ===");
        System.out.println("=== Product ID: " + product.getId() + " ===");
        System.out.println("=== User ID: " + product.getUserId() + " ===");
        
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Kiểm tra balance trước khi đăng bài
        repository.checkBalanceForPosting(product.getUserId(), new ProductRepository.OnBalanceCheckListener() {
            @Override
            public void onSufficientBalance() {
                // Balance đủ, tiến hành đăng bài và trừ phí
                repository.insertProduct(product, imagePaths, new ProductRepository.OnProductOperationListener() {
                    @Override
                    public void onSuccess() {
                        // Sau khi đăng bài thành công, trừ phí và tạo transaction
                        repository.deductPostingFeeAndCreateTransaction(product.getUserId(), product.getId(), new ProductRepository.OnProductOperationListener() {
                            @Override
                            public void onSuccess() {
                                // Reload products after adding new one (with visibility filtering)
                                loadProductsForCurrentUser(product.getUserId());
                                if (listener != null) {
                                    listener.onSuccess();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                errorMessage.setValue("Đăng bài thành công nhưng có lỗi khi trừ phí: " + error);
                                isLoading.setValue(false);
                                if (listener != null) {
                                    listener.onError(error);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        errorMessage.setValue(error);
                        isLoading.setValue(false);
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                });
            }

            @Override
            public void onInsufficientBalance(double currentBalance, double requiredAmount) {
                errorMessage.setValue("Số dư không đủ để đăng bài. Cần: " + requiredAmount + " VNĐ, Hiện có: " + currentBalance + " VNĐ");
                isLoading.setValue(false);
                if (listener != null) {
                    listener.onInsufficientBalance(currentBalance, requiredAmount);
                }
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Lỗi kiểm tra số dư: " + error);
                isLoading.setValue(false);
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }

    public void updateProduct(Product product, List<String> newImagePaths, OnProductUpdateListener listener) {
        System.out.println("=== ProductViewModel: Starting updateProduct ===");
        System.out.println("=== Product ID: " + product.getId() + " ===");
        System.out.println("=== User ID: " + product.getUserId() + " ===");
        
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.updateProduct(product, newImagePaths, new ProductRepository.OnProductOperationListener() {
            @Override
            public void onSuccess() {
                System.out.println("=== ProductViewModel: Product updated successfully ===");
                // Reload products after updating (with visibility filtering)
                loadProductsForCurrentUser(product.getUserId());
                
                // Also reload all products to update the main product list
                // Note: loadProducts() is called from HomeFragment, so we don't need to call it here
                // The HomeFragment will be notified via onResume() in MainActivity
                
                isLoading.setValue(false);
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("=== ProductViewModel: Error updating product: " + error + " ===");
                errorMessage.setValue(error);
                isLoading.setValue(false);
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }

    public void deleteProduct(String productId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.deleteProduct(productId, new ProductRepository.OnProductOperationListener() {
            @Override
            public void onSuccess() {
                // Reload products after deleting (with visibility filtering)
                // We need to get the current user ID from the current products list
                if (products.getValue() != null && !products.getValue().isEmpty()) {
                    int currentUserId = products.getValue().get(0).getUserId();
                    loadProductsForCurrentUser(currentUserId);
                } else {
                    // If no products, just clear the list
                    products.setValue(new ArrayList<>());
                    productImagesList.setValue(new ArrayList<>());
                    userNames.setValue(new HashMap<>());
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void increaseProductViews(String productId) {
        repository.increaseProductViews(productId, new ProductRepository.OnProductOperationListener() {
            @Override
            public void onSuccess() {
                // Views updated successfully, no need to reload all products
                System.out.println("Product views increased successfully for ID: " + productId);
            }

            @Override
            public void onError(String error) {
                System.out.println("Failed to increase product views: " + error);
                // Don't show error to user as this is a background operation
            }
        });
    }

    public void loadProductImages(String productId, OnProductImagesLoadedListener listener) {
        System.out.println("=== ProductViewModel: Loading images for product: " + productId + " ===");
        
        repository.loadProductImages(productId, new ProductRepository.OnProductImagesLoadedListener() {
            @Override
            public void onImagesLoaded(List<ProductImages> images) {
                System.out.println("=== ProductViewModel: Images loaded successfully, count: " + (images != null ? images.size() : 0) + " ===");
                if (listener != null) {
                    listener.onImagesLoaded(images);
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("=== ProductViewModel: Error loading images: " + error + " ===");
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }

    public void loadProductDetails(String productId, OnProductDetailsLoadedListener listener) {
        System.out.println("=== ProductViewModel: Loading product details for ID: " + productId + " ===");
        
        repository.loadProductDetails(productId, new ProductRepository.OnProductDetailsLoadedListener() {
            @Override
            public void onProductDetailsLoaded(Product product, List<ProductImages> images) {
                System.out.println("=== ProductViewModel: Product details loaded successfully ===");
                if (listener != null) {
                    listener.onProductDetailsLoaded(product, images);
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("=== ProductViewModel: Error loading product details: " + error + " ===");
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
    
    public void checkProductSoldStatus(String productId, OnProductSoldStatusListener listener) {
        System.out.println("=== ProductViewModel: Checking sold status for product: " + productId + " ===");
        
        repository.checkProductSoldStatus(productId, new ProductRepository.OnProductSoldStatusListener() {
            @Override
            public void onProductSoldStatusChecked(boolean sold) {
                System.out.println("=== ProductViewModel: Product sold status: " + sold + " ===");
                if (listener != null) {
                    listener.onProductSoldStatusChecked(sold);
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("=== ProductViewModel: Error checking sold status: " + error + " ===");
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }
}
