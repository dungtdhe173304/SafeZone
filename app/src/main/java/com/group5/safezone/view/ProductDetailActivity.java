package com.group5.safezone.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group5.safezone.R;
import com.group5.safezone.adapter.ProductAdapter;
import com.group5.safezone.config.PasswordUtils;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.util.ProductDisplayHelper;
import com.group5.safezone.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "product_description";
    public static final String EXTRA_PRODUCT_INFORMATION = "product_information";
    public static final String EXTRA_PRODUCT_PRICE = "product_price";
    public static final String EXTRA_PRODUCT_FEE = "product_fee";
    public static final String EXTRA_PRODUCT_STATUS = "product_status";
    public static final String EXTRA_PRODUCT_PUBLIC_PRIVATE = "product_public_private";
    public static final String EXTRA_PRODUCT_PRIVATE_INFO = "product_private_info";
    public static final String EXTRA_PRODUCT_USER_VIEW = "product_user_view";
    public static final String EXTRA_PRODUCT_USER_ID = "product_user_id";
    public static final String EXTRA_PRODUCT_CREATED_AT = "product_created_at";
    public static final String EXTRA_PRODUCT_VIEWS = "product_views";
    public static final String EXTRA_SELLER_USERNAME = "seller_username";

    private Product product;
    private List<ProductImages> productImages;
    private String sellerUsername;
    private SessionManager session;
    private ProductViewModel productViewModel;
    private boolean isPrivateInfoVisible = false;
    private boolean isProductSold = false; // Track if product is sold
    
    private static final int REQUEST_EDIT_PRODUCT = 1001;

    // UI Elements
    private RecyclerView rvProductImages;
    private TextView tvProductName, tvDescription, tvInformation, tvPrice, tvFee, tvStatus;
    private TextView tvPublicPrivate, tvSellerUsername, tvTimePosted, tvViews, tvPrivateInfo;
    private ImageView ivSellerAvatar, ivTogglePrivateInfo;
    private LinearLayout llPrivateInfoSection;
    private Button btnBuyNow, btnEdit;
    private ProductAdapter.ProductImagesAdapter imagesAdapter;

    public static Intent newIntent(Context context, Product product, List<ProductImages> images, String sellerUsername) {
        System.out.println("=== ProductDetailActivity: Creating intent for product: " + product.getId() + " ===");
        System.out.println("=== Product userView: " + product.getUserView() + " ===");
        System.out.println("=== Product publicPrivate: " + product.getPublicPrivate() + " ===");
        
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(EXTRA_PRODUCT_NAME, product.getProductName());
        intent.putExtra(EXTRA_PRODUCT_DESCRIPTION, product.getDescribe());
        intent.putExtra(EXTRA_PRODUCT_INFORMATION, product.getInformation());
        intent.putExtra(EXTRA_PRODUCT_PRICE, product.getPrice());
        intent.putExtra(EXTRA_PRODUCT_FEE, product.getFee());
        intent.putExtra(EXTRA_PRODUCT_STATUS, product.getStatus());
        intent.putExtra(EXTRA_PRODUCT_PUBLIC_PRIVATE, product.getPublicPrivate());
        intent.putExtra(EXTRA_PRODUCT_PRIVATE_INFO, product.getPrivateInfo());
        intent.putExtra(EXTRA_PRODUCT_USER_VIEW, product.getUserView());
        intent.putExtra(EXTRA_PRODUCT_USER_ID, product.getUserId());
        intent.putExtra(EXTRA_PRODUCT_CREATED_AT, product.getCreatedAt().getTime());
        intent.putExtra(EXTRA_PRODUCT_VIEWS, product.getView());
        intent.putExtra(EXTRA_SELLER_USERNAME, sellerUsername);
        
        System.out.println("=== ProductDetailActivity: Intent created with userView: " + product.getUserView() + " ===");
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        session = new SessionManager(this);
        productViewModel = new ProductViewModel(getApplication());
        initViews();
        setupToolbar();
        loadProductData();
        checkProductSoldStatus();
        setupPrivateInfoToggle();
        setupBuyNowButton();
        setupEditButton();
        setupImageClickHandling();
        
        // Increase product views when activity is opened
        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId != null) {
            productViewModel.increaseProductViews(productId);
        }
    }

    private void initViews() {
        rvProductImages = findViewById(R.id.rvProductImages);
        tvProductName = findViewById(R.id.tvProductName);
        tvDescription = findViewById(R.id.tvDescription);
        tvInformation = findViewById(R.id.tvInformation);
        tvPrice = findViewById(R.id.tvPrice);
        tvFee = findViewById(R.id.tvFee);
        tvStatus = findViewById(R.id.tvStatus);
        tvPublicPrivate = findViewById(R.id.tvPublicPrivate);
        tvSellerUsername = findViewById(R.id.tvSellerUsername);
        tvTimePosted = findViewById(R.id.tvTimePosted);
        tvViews = findViewById(R.id.tvViews);
        tvPrivateInfo = findViewById(R.id.tvPrivateInfo);
        ivSellerAvatar = findViewById(R.id.ivSellerAvatar);
        ivTogglePrivateInfo = findViewById(R.id.ivTogglePrivateInfo);
        llPrivateInfoSection = findViewById(R.id.llPrivateInfoSection);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnEdit = findViewById(R.id.btnEdit);

        // Setup RecyclerView for images
        rvProductImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new ProductAdapter.ProductImagesAdapter();
        rvProductImages.setAdapter(imagesAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            // Add edit button to toolbar if user is the product owner
            int productUserId = getIntent().getIntExtra(EXTRA_PRODUCT_USER_ID, 0);
            if (productUserId == session.getUserId()) {
                getSupportActionBar().setTitle("Chi tiết sản phẩm (Có thể chỉnh sửa)");
            } else {
                getSupportActionBar().setTitle("Chi tiết sản phẩm");
            }
        }
    }

    private void loadProductData() {
        // Get data from intent
        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        String description = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);
        String information = getIntent().getStringExtra(EXTRA_PRODUCT_INFORMATION);
        Double price = getIntent().getDoubleExtra(EXTRA_PRODUCT_PRICE, 0.0);
        Double fee = getIntent().getDoubleExtra(EXTRA_PRODUCT_FEE, 0.0);
        String status = getIntent().getStringExtra(EXTRA_PRODUCT_STATUS);
        String publicPrivate = getIntent().getStringExtra(EXTRA_PRODUCT_PUBLIC_PRIVATE);
        String privateInfo = getIntent().getStringExtra(EXTRA_PRODUCT_PRIVATE_INFO);
        String userView = getIntent().getStringExtra(EXTRA_PRODUCT_USER_VIEW);
        int productUserId = getIntent().getIntExtra(EXTRA_PRODUCT_USER_ID, 0);
        long createdAt = getIntent().getLongExtra(EXTRA_PRODUCT_CREATED_AT, 0);
        Integer views = getIntent().getIntExtra(EXTRA_PRODUCT_VIEWS, 0);
        sellerUsername = getIntent().getStringExtra(EXTRA_SELLER_USERNAME);

        // Create Product object from intent data
        product = new Product();
        product.setId(productId);
        product.setProductName(productName);
        product.setDescribe(description);
        product.setInformation(information);
        product.setPrice(price);
        product.setFee(fee);
        product.setStatus(status);
        product.setPublicPrivate(publicPrivate);
        product.setPrivateInfo(privateInfo);
        product.setUserView(userView);
        product.setUserId(productUserId);
        product.setCreatedAt(new java.util.Date(createdAt));
        product.setView(views);
        
        System.out.println("=== ProductDetailActivity: Product object created successfully ===");
        System.out.println("=== Product ID: " + product.getId() + " ===");
        System.out.println("=== Product Name: " + product.getProductName() + " ===");
        System.out.println("=== User ID: " + product.getUserId() + " ===");
        System.out.println("=== Product userView: " + product.getUserView() + " ===");
        System.out.println("=== Product publicPrivate: " + product.getPublicPrivate() + " ===");

        // Set product data
        tvProductName.setText(productName);
        tvPrice.setText(ProductDisplayHelper.formatPrice(price));
        tvFee.setText(ProductDisplayHelper.formatFee(fee));
        tvStatus.setText("Status: " + (status != null ? status : "Active"));
        tvViews.setText(ProductDisplayHelper.formatViews(views));

        // Set description
        if (description != null && !description.trim().isEmpty()) {
            tvDescription.setText(description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        // Set information
        if (information != null && !information.trim().isEmpty()) {
            tvInformation.setText(information);
            tvInformation.setVisibility(View.VISIBLE);
        } else {
            tvInformation.setVisibility(View.GONE);
        }

        // Set public/private status
        if ("public".equals(publicPrivate)) {
            tvPublicPrivate.setText(getString(R.string.visibility_public));
            tvPublicPrivate.setBackgroundResource(R.drawable.status_success_background);
        } else {
            tvPublicPrivate.setText(getString(R.string.visibility_private));
            tvPublicPrivate.setBackgroundResource(R.drawable.bg_role_tag);
        }

        // Set seller info
        if (sellerUsername != null) {
            tvSellerUsername.setText(sellerUsername);
        } else {
            tvSellerUsername.setText("User " + productUserId);
        }

        // Set time posted
        if (createdAt > 0) {
            tvTimePosted.setText(ProductDisplayHelper.formatTimeAgo(new java.util.Date(createdAt)));
        }

        // Setup private info section (only for seller)
        if (productUserId == session.getUserId() && privateInfo != null && !privateInfo.trim().isEmpty()) {
            llPrivateInfoSection.setVisibility(View.VISIBLE);
            tvPrivateInfo.setText("Nhấn vào biểu tượng mắt để xem thông tin riêng tư");
        } else {
            llPrivateInfoSection.setVisibility(View.GONE);
        }



        // Set seller avatar
        ivSellerAvatar.setImageResource(R.drawable.ic_person);

        // Load product images from intent
        loadProductImagesFromIntent();
    }
    
    private void checkProductSoldStatus() {
        if (product != null && product.getId() != null) {
            // Check if product has any orders
            productViewModel.checkProductSoldStatus(product.getId(), new ProductViewModel.OnProductSoldStatusListener() {
                @Override
                public void onProductSoldStatusChecked(boolean sold) {
                    isProductSold = sold;
                    runOnUiThread(() -> {
                        updateButtonVisibility();
                    });
                }
                
                @Override
                public void onError(String error) {
                    System.out.println("=== ProductDetailActivity: Error checking sold status: " + error + " ===");
                    // Default to not sold on error
                    isProductSold = false;
                    updateButtonVisibility();
                }
            });
        }
    }
    
    private void updateButtonVisibility() {
        if (product != null) {
            int productUserId = product.getUserId();
            int currentUserId = session.getUserId();
            
            // Show Edit button only for product owner and if product is not sold
            if (productUserId == currentUserId && !isProductSold) {
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                btnEdit.setVisibility(View.GONE);
            }
            
            // Show Buy Now button only for non-owners and if product is not sold
            if (productUserId != currentUserId && !isProductSold) {
                btnBuyNow.setVisibility(View.VISIBLE);
            } else {
                btnBuyNow.setVisibility(View.GONE);
            }
            
            // Update toolbar title
            if (getSupportActionBar() != null) {
                if (productUserId == currentUserId) {
                    if (isProductSold) {
                        getSupportActionBar().setTitle("Chi tiết sản phẩm (Đã bán)");
                    } else {
                        getSupportActionBar().setTitle("Chi tiết sản phẩm (Có thể chỉnh sửa)");
                    }
                } else {
                    if (isProductSold) {
                        getSupportActionBar().setTitle("Chi tiết sản phẩm (Đã bán)");
                    } else {
                        getSupportActionBar().setTitle("Chi tiết sản phẩm");
                    }
                }
            }
        }
    }

    private void loadProductImagesFromIntent() {
        // Get images from intent extras
        if (productImages != null && !productImages.isEmpty()) {
            imagesAdapter.updateImages(productImages);
            rvProductImages.setVisibility(View.VISIBLE);
            System.out.println("ProductDetail: Loaded " + productImages.size() + " images");
        } else {
            // Try to get images from intent extras
            String imagePaths = getIntent().getStringExtra("product_image_paths");
            if (imagePaths != null && !imagePaths.isEmpty()) {
                // Parse comma-separated image paths
                String[] paths = imagePaths.split(",");
                List<ProductImages> images = new ArrayList<>();
                for (String path : paths) {
                    if (path.trim().length() > 0) {
                        ProductImages img = new ProductImages();
                        img.setPath(path.trim());
                        images.add(img);
                    }
                }
                if (!images.isEmpty()) {
                    imagesAdapter.updateImages(images);
                    rvProductImages.setVisibility(View.VISIBLE);
                    System.out.println("ProductDetail: Loaded " + images.size() + " images from intent");
                } else {
                    rvProductImages.setVisibility(View.GONE);
                }
            } else {
                rvProductImages.setVisibility(View.GONE);
                System.out.println("ProductDetail: No images found");
            }
        }
    }

    private void setupPrivateInfoToggle() {
        ivTogglePrivateInfo.setOnClickListener(v -> {
            if (isPrivateInfoVisible) {
                hidePrivateInfo();
            } else {
                showPrivateInfo();
            }
        });
    }

    private void setupBuyNowButton() {
        btnBuyNow.setOnClickListener(v -> {
            // Handle buy now action
            Toast.makeText(this, "Chức năng mua hàng sẽ được phát triển sau", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupEditButton() {
        // Show Edit button only for product owner
        int productUserId = getIntent().getIntExtra(EXTRA_PRODUCT_USER_ID, 0);
        if (productUserId == session.getUserId()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> {
                // Check if product object is ready
                if (product != null) {
                    System.out.println("=== ProductDetailActivity: Opening EditProductActivity for product: " + product.getId() + " ===");
                    System.out.println("=== Product userView: " + product.getUserView() + " ===");
                    System.out.println("=== Product publicPrivate: " + product.getPublicPrivate() + " ===");
                    Intent intent = EditProductActivity.newIntent(this, product, productImages);
                    startActivityForResult(intent, REQUEST_EDIT_PRODUCT);
                } else {
                    System.out.println("=== ProductDetailActivity: ERROR - Product object is null! ===");
                    Toast.makeText(this, "Không thể mở trang chỉnh sửa - Dữ liệu sản phẩm không hợp lệ", Toast.LENGTH_LONG).show();
                }
            });
        }
        
        // Setup Buy Now button for non-owners (only if not sold)
        if (productUserId != session.getUserId()) {
            btnBuyNow.setOnClickListener(v -> {
                if (product != null) {
                    // Open buy product activity
                    Intent intent = new Intent(this, BuyProductActivity.class);
                    intent.putExtra(BuyProductActivity.EXTRA_PRODUCT_ID, product.getId());
                    intent.putExtra(BuyProductActivity.EXTRA_PRODUCT_PRICE, product.getPrice());
                    intent.putExtra(BuyProductActivity.EXTRA_SELLER_USERNAME, sellerUsername);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Không thể mở trang mua hàng - Dữ liệu sản phẩm không hợp lệ", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setupImageClickHandling() {
        // Set click listener for images in the adapter
        if (imagesAdapter != null) {
            imagesAdapter.setOnImageClickListener(new ProductAdapter.ProductImagesAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(ProductImages image, int position) {
                    openFullScreenImageViewer(position);
                }
            });
        }
    }

    private void openFullScreenImageViewer(int startPosition) {
        if (productImages == null || productImages.isEmpty()) {
            Toast.makeText(this, "Không có ảnh để xem", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and show ImageViewerDialog
        ImageViewerDialog dialog = ImageViewerDialog.newInstance(productImages, startPosition);
        dialog.show(getSupportFragmentManager(), "ImageViewerDialog");
    }

    private void showPrivateInfo() {
        String encryptedPrivateInfo = getIntent().getStringExtra(EXTRA_PRODUCT_PRIVATE_INFO);
        if (encryptedPrivateInfo != null && !encryptedPrivateInfo.trim().isEmpty()) {
            try {
                String decryptedInfo = PasswordUtils.decrypt(encryptedPrivateInfo);
                tvPrivateInfo.setText(decryptedInfo);
                tvPrivateInfo.setVisibility(View.VISIBLE);
                ivTogglePrivateInfo.setImageResource(R.drawable.ic_visibility);
                isPrivateInfoVisible = true;
            } catch (Exception e) {
                Toast.makeText(this, "Không thể giải mã thông tin riêng tư", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hidePrivateInfo() {
        tvPrivateInfo.setVisibility(View.GONE);
        ivTogglePrivateInfo.setImageResource(R.drawable.ic_visibility_off);
        isPrivateInfoVisible = false;
    }

    public void setProductImages(List<ProductImages> images) {
        this.productImages = images;
        if (images != null && !images.isEmpty()) {
            imagesAdapter.updateImages(images);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_EDIT_PRODUCT && resultCode == RESULT_OK && data != null) {
            boolean productUpdated = data.getBooleanExtra("PRODUCT_UPDATED", false);
            if (productUpdated) {
                System.out.println("=== ProductDetailActivity: Product was updated, reloading data ===");
                
                // Reload product data from database
                reloadProductDataFromDatabase();
            }
        }
    }
    
    private void reloadProductDataFromDatabase() {
        System.out.println("=== ProductDetailActivity: Reloading product data from database ===");
        
        if (product != null && product.getId() != null) {
            // Load updated product data from database
            productViewModel.loadProductDetails(product.getId(), new ProductViewModel.OnProductDetailsLoadedListener() {
                @Override
                public void onProductDetailsLoaded(Product updatedProduct, List<ProductImages> updatedImages) {
                    if (updatedProduct != null) {
                        // Update local product object
                        product = updatedProduct;
                        
                        // Update UI with new data
                        updateUIWithProductData(updatedProduct);
                        
                        // Update images
                        if (updatedImages != null) {
                            productImages = updatedImages;
                            updateImagesUI(updatedImages);
                        }
                        
                        System.out.println("=== ProductDetailActivity: Product data reloaded successfully ===");
                    }
                }
                
                @Override
                public void onError(String error) {
                    System.out.println("=== ProductDetailActivity: Error reloading product data: " + error + " ===");
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải lại dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void updateUIWithProductData(Product updatedProduct) {
        // Update UI elements with new product data
        tvProductName.setText(updatedProduct.getProductName());
        tvPrice.setText(ProductDisplayHelper.formatPrice(updatedProduct.getPrice()));
        tvFee.setText(ProductDisplayHelper.formatFee(updatedProduct.getFee()));
        
        // Update description
        if (updatedProduct.getDescribe() != null && !updatedProduct.getDescribe().trim().isEmpty()) {
            tvDescription.setText(updatedProduct.getDescribe());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
        
        // Update information
        if (updatedProduct.getInformation() != null && !updatedProduct.getInformation().trim().isEmpty()) {
            tvInformation.setText(updatedProduct.getInformation());
            tvInformation.setVisibility(View.VISIBLE);
        } else {
            tvInformation.setVisibility(View.GONE);
        }
        
        // Update public/private status
        if ("public".equals(updatedProduct.getPublicPrivate())) {
            tvPublicPrivate.setText(getString(R.string.visibility_public));
            tvPublicPrivate.setBackgroundResource(R.drawable.status_success_background);
        } else {
            tvPublicPrivate.setText(getString(R.string.visibility_private));
            tvPublicPrivate.setBackgroundResource(R.drawable.bg_role_tag);
        }
        
        // Update private info section visibility
        if ("private".equals(updatedProduct.getPublicPrivate())) {
            llPrivateInfoSection.setVisibility(View.VISIBLE);
        } else {
            llPrivateInfoSection.setVisibility(View.GONE);
        }
    }

    private void updateImagesUI(List<ProductImages> images) {
        if (images != null && !images.isEmpty()) {
            // Update images adapter
            if (imagesAdapter != null) {
                imagesAdapter.updateImages(images);
            }
            
            // Show images section
            rvProductImages.setVisibility(View.VISIBLE);
        } else {
            // Hide images section if no images
            rvProductImages.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
