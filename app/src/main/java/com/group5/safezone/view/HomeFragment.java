package com.group5.safezone.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.group5.safezone.R;
import com.group5.safezone.adapter.ImagePreviewAdapter;
import com.group5.safezone.adapter.ProductAdapter;
import com.group5.safezone.config.PasswordUtils;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.viewmodel.ProductViewModel;
import com.group5.safezone.view.Wallet.WalletActivity;
import com.group5.safezone.view.EditProductActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private ProductViewModel productViewModel;
    private ProductAdapter productAdapter;
    private ImagePreviewAdapter imagePreviewAdapter;
    private final List<String> selectedImagePaths = new ArrayList<>();

    // Dialog views
    private EditText etProductName, etDescription, etInformation, etPrice, etPrivateInfo, etUserView;
    private RadioGroup rgFee, rgPublicPrivate;
    private RadioButton rbSellerFee, rbBuyerFee, rbPublic, rbPrivate;
    private TextInputLayout tilUserView;
    private Button btnAddImages, btnAddProduct;
    private RecyclerView rvImagePreview;

    // Main fragment views
    private Button btnOpenAddProduct;
    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private LinearLayout llProductsLoading, llEmptyState;
    private TextView tvProductCount;
    private TextInputEditText etSearchProduct;
    private ImageButton btnClearSearch;
    private ImageButton btnFilter;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 200;

    private UserDao userDao;
    private ExecutorService executor;
    private SessionManager sessionManager;
    private int currentUserId = -1; // Will be loaded from login session
    
    // Original data for search functionality
    private List<Product> originalProducts;
    private List<List<ProductImages>> originalProductImagesList;
    private Map<Integer, String> originalUserNames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize database and executor
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(requireContext());
        
        initViews(view);
        setupListeners();
        
        // Load current user ID from login session FIRST
        loadCurrentUserId();
        
        // Setup RecyclerViews AFTER currentUserId is loaded
        setupRecyclerViews();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        // loadProducts() sẽ được gọi sau khi loadCurrentUserId() hoàn thành
    }

    private void initViews(View view) {
        btnOpenAddProduct = view.findViewById(R.id.btnOpenAddProduct);
        rvProducts = view.findViewById(R.id.rvProducts);
        progressBar = view.findViewById(R.id.progressBar);
        llProductsLoading = view.findViewById(R.id.llProductsLoading);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        etSearchProduct = view.findViewById(R.id.etSearchProduct);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        tvProductCount = view.findViewById(R.id.tvProductCount);
    }

    private void setupRecyclerViews() {
        productAdapter = new ProductAdapter(new ArrayList<>(), new ArrayList<>(), new HashMap<>(), currentUserId, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Open product detail activity
                openProductDetail(product);
            }

            @Override
            public void onDeleteClick(Product product) {
                // Handle delete click
                new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_product))
                    .setMessage(getString(R.string.delete_product_confirmation))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        productViewModel.deleteProduct(product.getId());
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            }

            @Override
            public void onEditClick(Product product) {
                openEditProductDialog(product);
            }
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnOpenAddProduct.setOnClickListener(v -> showAddProductDialog());
        
        // Setup search functionality
        setupSearchFunctionality();
    }
    
    private void setupSearchFunctionality() {
        // Search text change listener
        etSearchProduct.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();
                if (searchQuery.isEmpty()) {
                    btnClearSearch.setVisibility(View.GONE);
                    // Show all products
                    if (productAdapter != null) {
                        productAdapter.updateData(originalProducts, originalProductImagesList, originalUserNames);
                    }
                } else {
                    btnClearSearch.setVisibility(View.VISIBLE);
                    // Filter products
                    filterProducts(searchQuery);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Clear search button
        btnClearSearch.setOnClickListener(v -> {
            etSearchProduct.setText("");
            btnClearSearch.setVisibility(View.GONE);
            // Show all products
            if (productAdapter != null) {
                productAdapter.updateData(originalProducts, originalProductImagesList, originalUserNames);
            }
        });
        
        // Filter button
        btnFilter.setOnClickListener(v -> {
            // TODO: Implement filter functionality
            Toast.makeText(getContext(), "Tính năng lọc đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void filterProducts(String query) {
        if (originalProducts == null || originalProductImagesList == null || originalUserNames == null) {
            return;
        }
        
        List<Product> filteredProducts = new ArrayList<>();
        List<List<ProductImages>> filteredProductImagesList = new ArrayList<>();
        Map<Integer, String> filteredUserNames = new HashMap<>();
        
        for (int i = 0; i < originalProducts.size(); i++) {
            Product product = originalProducts.get(i);
            
            // Search in product name, description, information
            boolean matches = product.getProductName().toLowerCase().contains(query.toLowerCase()) ||
                            (product.getDescribe() != null && product.getDescribe().toLowerCase().contains(query.toLowerCase())) ||
                            (product.getInformation() != null && product.getInformation().toLowerCase().contains(query.toLowerCase()));
            
            if (matches) {
                filteredProducts.add(product);
                filteredProductImagesList.add(originalProductImagesList.get(i));
                filteredUserNames.put(product.getUserId(), originalUserNames.get(product.getUserId()));
            }
        }
        
        if (productAdapter != null) {
            productAdapter.updateData(filteredProducts, filteredProductImagesList, filteredUserNames);
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.add_new_product));

        // Inflate dialog layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        initDialogViews(dialogView);
        setupDialogListeners();
        setupImagePreviewRecyclerView(dialogView);

        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.add_product), (dialog, which) -> {
            // This will be handled in onShow to prevent dialog from closing on validation error
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            clearDialogForm();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateAndAddProduct()) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void initDialogViews(View dialogView) {
        etProductName = dialogView.findViewById(R.id.etProductName);
        etDescription = dialogView.findViewById(R.id.etDescription);
        etInformation = dialogView.findViewById(R.id.etInformation);
        etPrice = dialogView.findViewById(R.id.etPrice);
        etPrivateInfo = dialogView.findViewById(R.id.etPrivateInfo);
        etUserView = dialogView.findViewById(R.id.etUserView);

        rgFee = dialogView.findViewById(R.id.rgFee);
        rbSellerFee = dialogView.findViewById(R.id.rbSellerFee);
        rbBuyerFee = dialogView.findViewById(R.id.rbBuyerFee);

        rgPublicPrivate = dialogView.findViewById(R.id.rgPublicPrivate);
        rbPublic = dialogView.findViewById(R.id.rbPublic);
        rbPrivate = dialogView.findViewById(R.id.rbPrivate);

        tilUserView = dialogView.findViewById(R.id.tilUserView);
        btnAddImages = dialogView.findViewById(R.id.btnAddImages);
        rvImagePreview = dialogView.findViewById(R.id.rvImagePreview);

        // Set default values
        rbSellerFee.setChecked(true);
        rbPublic.setChecked(true);
    }

    private void setupDialogListeners() {
        btnAddImages.setOnClickListener(v -> checkPermissionAndPickImages());

        rgPublicPrivate.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPrivate) {
                tilUserView.setVisibility(View.VISIBLE);
            } else {
                tilUserView.setVisibility(View.GONE);
                etUserView.setText("");
            }
        });
    }

    private void setupImagePreviewRecyclerView(View dialogView) {
        imagePreviewAdapter = new ImagePreviewAdapter();
        rvImagePreview.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImagePreview.setAdapter(imagePreviewAdapter);
        rvImagePreview.setVisibility(View.GONE);
    }

    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        System.out.println("=== HomeFragment: ProductViewModel initialized ===");

        observeViewModel();
        
        // Now that ViewModel is ready, load products if we have user ID
        if (currentUserId > 0) {
            System.out.println("=== HomeFragment: Loading products after ViewModel setup ===");
            loadProducts();
        }
    }

    private void observeViewModel() {
        // Main observer - only update when all data is ready
        productViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                // Check if we have images and usernames ready
                List<List<ProductImages>> images = productViewModel.getProductImagesList().getValue();
                Map<Integer, String> userNames = productViewModel.getUserNames().getValue();
                
                if (images != null && userNames != null) {
                    // Store original data for search functionality
                    this.originalProducts = new ArrayList<>(products);
                    this.originalProductImagesList = new ArrayList<>(images);
                    this.originalUserNames = new HashMap<>(userNames);
                    
                    productAdapter.updateData(products, images, userNames);
                    
                    // Check sold status for all products and update adapter
                    checkProductsSoldStatus(products);
                    
                    updateProductCount(products.size());
                    showProductsList();
                }
                // If not ready yet, wait for the other observers
            } else {
                productAdapter.updateData(new ArrayList<>(), new ArrayList<>(), new HashMap<>());
                updateProductCount(0);
                showEmptyState();
            }
        });

        // Secondary observers - only update when main observer is ready
        productViewModel.getProductImagesList().observe(getViewLifecycleOwner(), productImagesList -> {
            // Only update if we already have products and usernames
            List<Product> products = this.originalProducts;
            Map<Integer, String> userNames = this.originalUserNames;
            
            if (products != null && !products.isEmpty() && productImagesList != null && userNames != null) {
                // Update original data and adapter
                this.originalProductImagesList = new ArrayList<>(productImagesList);
                productAdapter.updateData(products, productImagesList, userNames);
            }
        });

        productViewModel.getUserNames().observe(getViewLifecycleOwner(), userNames -> {
            // Only update if we already have products and images
            List<Product> products = this.originalProducts;
            List<List<ProductImages>> images = this.originalProductImagesList;
            
            if (products != null && !products.isEmpty() && images != null && userNames != null) {
                // Update original data and adapter
                this.originalUserNames = new HashMap<>(userNames);
                productAdapter.updateData(products, images, userNames);
            }
        });

        productViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoadingState();
            }
            // Hide progress bar when loading is done
            progressBar.setVisibility(View.GONE);
        });

        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                // Log error for debugging
                System.out.println("ProductViewModel Error: " + errorMessage);
            }
        });
    }

    private void showLoadingState() {
        llProductsLoading.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
    }

    private void showProductsList() {
        llProductsLoading.setVisibility(View.GONE);
        rvProducts.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        llProductsLoading.setVisibility(View.GONE);
        rvProducts.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
    }
    
    private void updateProductCount(int count) {
        if (tvProductCount != null) {
            tvProductCount.setText(count + " sản phẩm");
        }
    }
    
    private void checkProductsSoldStatus(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        
        // Create a map to store sold status for each product
        Map<String, Boolean> productSoldStatus = new HashMap<>();
        
        // Check sold status for each product
        for (Product product : products) {
            checkSingleProductSoldStatus(product.getId(), productSoldStatus, products.size());
        }
    }
    
    private void checkSingleProductSoldStatus(String productId, Map<String, Boolean> productSoldStatus, int totalProducts) {
        // Use a simple approach: check if product has any orders
        executor.execute(() -> {
            try {
                com.group5.safezone.model.dao.OrderDao orderDao = AppDatabase.getDatabase(requireContext()).orderDao();
                List<com.group5.safezone.model.entity.Order> orders = orderDao.getOrdersByProductId(productId);
                
                boolean isSold = orders != null && !orders.isEmpty();
                productSoldStatus.put(productId, isSold);
                
                // Update adapter when all products are checked
                if (productSoldStatus.size() == totalProducts) {
                    requireActivity().runOnUiThread(() -> {
                        if (productAdapter != null) {
                            productAdapter.updateProductSoldStatus(productSoldStatus);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("=== HomeFragment: Error checking sold status for product " + productId + ": " + e.getMessage() + " ===");
                // Mark as not sold on error
                productSoldStatus.put(productId, false);
                
                // Update adapter when all products are checked
                if (productSoldStatus.size() == totalProducts) {
                    requireActivity().runOnUiThread(() -> {
                        if (productAdapter != null) {
                            productAdapter.updateProductSoldStatus(productSoldStatus);
                        }
                    });
                }
            }
        });
    }

    private void loadCurrentUserId() {
        try {
            // Get current user ID from login session
            if (sessionManager.isLoggedIn()) {
                currentUserId = sessionManager.getUserId();
                System.out.println("=== HomeFragment: Current user ID from session: " + currentUserId + " ===");
                
                // Update adapter with new currentUserId
                if (productAdapter != null) {
                    productAdapter.updateCurrentUserId(currentUserId);
                    System.out.println("=== HomeFragment: Updated adapter with currentUserId: " + currentUserId + " ===");
                }
                
                // Load products after getting user ID (only if ViewModel is ready)
                if (productViewModel != null) {
                    loadProducts();
                } else {
                    System.out.println("=== HomeFragment: ProductViewModel not ready yet, will load products later ===");
                }
            } else {
                // User not logged in, show error
                Toast.makeText(requireContext(), "Vui lòng đăng nhập trước", Toast.LENGTH_LONG).show();
                System.out.println("=== HomeFragment: User not logged in ===");
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi load user session: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("=== HomeFragment: Error loading user session: " + e.getMessage() + " ===");
        }
    }

    private void loadProducts() {
        System.out.println("=== HomeFragment: loadProducts() called ===");
        System.out.println("=== Current User ID: " + currentUserId + " ===");
        System.out.println("=== ProductViewModel: " + (productViewModel != null ? "Ready" : "NULL") + " ===");
        
        if (currentUserId > 0 && productViewModel != null) {
            System.out.println("=== HomeFragment: Calling productViewModel.loadProductsForCurrentUser(" + currentUserId + ") ===");
            productViewModel.loadProductsForCurrentUser(currentUserId);
        } else {
            System.out.println("=== HomeFragment: Cannot load products - User ID: " + currentUserId + ", ViewModel: " + (productViewModel != null ? "Ready" : "NULL") + " ===");
        }
    }

    private boolean validateAndAddProduct() {
        String productName = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String information = etInformation.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String privateInfo = etPrivateInfo.getText().toString().trim();
        String userView = etUserView.getText().toString().trim();

        // Validate tên sản phẩm - không được null/empty
        if (productName.isEmpty()) {
            etProductName.setError(getString(R.string.product_name_required));
            etProductName.requestFocus();
            return false;
        }

        // Validate giá - phải lớn hơn 10.000 VNĐ
        if (priceStr.isEmpty()) {
            etPrice.setError(getString(R.string.price_required));
            etPrice.requestFocus();
            return false;
        }

        double price;
        try { 
            price = Double.parseDouble(priceStr); 
        } catch (NumberFormatException e) {
            etPrice.setError(getString(R.string.invalid_price_format));
            etPrice.requestFocus();
            return false;
        }

        if (price <= 10000) {
            etPrice.setError(getString(R.string.price_must_be_greater_than_10000));
            etPrice.requestFocus();
            return false;
        }

        // Validate private info - không được null/empty
        if (privateInfo.isEmpty()) {
            etPrivateInfo.setError(getString(R.string.private_info_required));
            etPrivateInfo.requestFocus();
            return false;
        }

        Product p = new Product();
        // Generate unique ID for new product
        p.setId("prod_" + System.currentTimeMillis() + "_" + currentUserId);
        p.setProductName(productName);
        p.setDescribe(description);
        p.setInformation(information);
        p.setPrice(price);
        p.setStatus("Active");
        p.setCreatedAt(new Date());
        p.setUpdatedAt(new Date());
        p.setUserId(currentUserId); // lấy đúng user hiện tại

        // Fee: 1 = người bán chịu, 2 = người mua chịu
        p.setFee(rbSellerFee.isChecked() ? 1.0 : 2.0);

        // Public / Private
        if (rbPublic.isChecked()) {
            p.setPublicPrivate("public");
            p.setUserView(null);
        } else {
            p.setPublicPrivate("private");
            // Nếu rỗng → chỉ người đăng xem được (mặc định backend/logic hiểu là owner-only)
            // Nếu có username → lưu username để check visibility
            p.setUserView(userView.isEmpty() ? null : userView);
        }

        // Private info đã được validate ở trên, giờ encrypt và set
        try {
            p.setPrivateInfo(PasswordUtils.encrypt(privateInfo));
        } catch (Exception e) {
            Toast.makeText(getContext(), getString(R.string.failed_to_encrypt), Toast.LENGTH_SHORT).show();
            return false;
        }

        System.out.println("=== HomeFragment: Starting to add product ===");
        System.out.println("=== Product ID: " + p.getId() + " ===");
        System.out.println("=== User ID: " + p.getUserId() + " ===");
        System.out.println("=== Product Name: " + p.getProductName() + " ===");
        
        productViewModel.addProduct(p, new ArrayList<>(selectedImagePaths), new ProductViewModel.OnProductAddListener() {
            @Override
            public void onSuccess() {
                System.out.println("=== HomeFragment: Product added successfully! ===");
                Toast.makeText(getContext(), getString(R.string.posting_success), Toast.LENGTH_LONG).show();
                clearDialogForm();
                
                // Reload products to show the new one
                loadProducts();
            }

            @Override
            public void onInsufficientBalance(double currentBalance, double requiredAmount) {
                // Hiển thị dialog thông báo không đủ tiền và đề xuất nạp tiền
                new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.insufficient_balance))
                    .setMessage(getString(R.string.posting_fee_message, requiredAmount, currentBalance))
                    .setPositiveButton(getString(R.string.deposit_money), (dialog, which) -> {
                        // Chuyển hướng đến WalletActivity
                        Intent intent = new Intent(getActivity(), WalletActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }

    private void clearDialogForm() {
        if (etProductName != null) etProductName.setText("");
        if (etDescription != null) etDescription.setText("");
        if (etInformation != null) etInformation.setText("");
        if (etPrice != null) etPrice.setText("");
        if (etPrivateInfo != null) etPrivateInfo.setText("");
        if (etUserView != null) etUserView.setText("");
        selectedImagePaths.clear();
        if (imagePreviewAdapter != null) {
            imagePreviewAdapter.setItems(selectedImagePaths);
            if (rvImagePreview != null) rvImagePreview.setVisibility(View.GONE);
        }
        if (rbSellerFee != null) rbSellerFee.setChecked(true);
        if (rbPublic != null) rbPublic.setChecked(true);
        if (tilUserView != null) tilUserView.setVisibility(View.GONE);
    }

    private void checkPermissionAndPickImages() {
        // Android 13+ dùng READ_MEDIA_IMAGES, cũ hơn dùng READ_EXTERNAL_STORAGE
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), perm)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{perm}, PERMISSION_REQUEST_CODE);
        } else {
            pickImages();
        }
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_images)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImages();
            } else {
                Toast.makeText(getContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                ClipData clip = data.getClipData();
                for (int i = 0; i < clip.getItemCount(); i++) {
                    Uri uri = clip.getItemAt(i).getUri();
                    selectedImagePaths.add(uri.toString());
                }
            } else if (data.getData() != null) {
                selectedImagePaths.add(data.getData().toString());
            }

            if (!selectedImagePaths.isEmpty()) {
                rvImagePreview.setVisibility(View.VISIBLE);
                imagePreviewAdapter.setItems(selectedImagePaths);
                Toast.makeText(getContext(),
                        getString(R.string.selected_images, selectedImagePaths.size()),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void openProductDetail(Product product) {
        // Get product images for this product
        List<ProductImages> images = null;
        if (productViewModel.getProductImagesList().getValue() != null) {
            int productIndex = -1;
            for (int i = 0; i < productViewModel.getProducts().getValue().size(); i++) {
                if (productViewModel.getProducts().getValue().get(i).getId().equals(product.getId())) {
                    productIndex = i;
                    break;
                }
            }
            if (productIndex >= 0 && productIndex < productViewModel.getProductImagesList().getValue().size()) {
                images = productViewModel.getProductImagesList().getValue().get(productIndex);
            }
        }

        // Get seller username
        String sellerUsername = null;
        if (productViewModel.getUserNames().getValue() != null) {
            sellerUsername = productViewModel.getUserNames().getValue().get(product.getUserId());
        }

        // Create image paths string for intent
        StringBuilder imagePaths = new StringBuilder();
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                if (i > 0) imagePaths.append(",");
                imagePaths.append(images.get(i).getPath());
            }
        }

        // Open ProductDetailActivity with all data
        Intent intent = ProductDetailActivity.newIntent(requireContext(), product, images, sellerUsername);
        intent.putExtra("product_image_paths", imagePaths.toString());
        startActivity(intent);
    }

    @Override
    public void onProductClick(Product product) {
        Toast.makeText(getContext(), "Clicked: " + product.getProductName(), Toast.LENGTH_SHORT).show();
        // TODO: chuyển sang màn chi tiết nếu cần
    }

    @Override
    public void onDeleteClick(Product product) {
        productViewModel.deleteProduct(product.getId());
        Toast.makeText(getContext(), getString(R.string.product_deleted), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Product product) {
        openEditProductDialog(product);
    }

    private void openEditProductDialog(Product product) {
        // Open EditProductActivity
        Intent intent = EditProductActivity.newIntent(getContext(), product, null);
        startActivity(intent);
    }
    
    // Method to reload products from outside (e.g., when returning from EditProductActivity)
    public void reloadProducts() {
        System.out.println("=== HomeFragment: reloadProducts() called ===");
        if (productViewModel != null && currentUserId > 0) {
            System.out.println("=== HomeFragment: Reloading products after edit ===");
            loadProducts();
        } else {
            System.out.println("=== HomeFragment: Cannot reload products - ViewModel or UserID not ready ===");
        }
    }
}
