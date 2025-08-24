package com.group5.safezone.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.ProductAdapter;
import com.group5.safezone.config.PasswordUtils;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.viewmodel.ProductViewModel;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "product_description";
    public static final String EXTRA_PRODUCT_INFORMATION = "product_information";
    public static final String EXTRA_PRODUCT_PRICE = "product_price";
    public static final String EXTRA_PRODUCT_FEE = "product_fee";
    public static final String EXTRA_PRODUCT_STATUS = "product_status";
    public static final String EXTRA_PRODUCT_PUBLIC_PRIVATE = "product_public_private";
    public static final String EXTRA_PRODUCT_PRIVATE_INFO = "product_private_info";
    public static final String EXTRA_PRODUCT_USER_ID = "product_user_id";
    public static final String EXTRA_PRODUCT_CREATED_AT = "product_created_at";
    public static final String EXTRA_PRODUCT_VIEWS = "product_views";
    public static final String EXTRA_PRODUCT_USER_VIEW = "product_user_view";

    private Product product;
    private List<ProductImages> currentImages;
    private List<String> newImagePaths;
    private SessionManager session;
    private ProductViewModel productViewModel;

    // UI Elements
    private RecyclerView rvCurrentImages;
    private TextInputEditText etProductName, etDescription, etInformation, etPrice, etPrivateInfo, etUserView;
    private RadioGroup rgFee, rgPublicPrivate;
    private RadioButton rbSellerFee, rbBuyerFee, rbPublic, rbPrivate;
    private TextInputLayout tilUserView;
    private Button btnAddImages, btnRemoveImages, btnCancel, btnSave;
    private ProductAdapter.ProductImagesAdapter imagesAdapter;

    private static final int REQUEST_IMAGE_PICK = 1001;

    public static Intent newIntent(android.content.Context context, Product product, List<ProductImages> images) {
        System.out.println("=== EditProductActivity: Creating intent for product: " + product.getId() + " ===");
        System.out.println("=== Product userView: " + product.getUserView() + " ===");
        System.out.println("=== Product publicPrivate: " + product.getPublicPrivate() + " ===");
        
        Intent intent = new Intent(context, EditProductActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(EXTRA_PRODUCT_NAME, product.getProductName());
        intent.putExtra(EXTRA_PRODUCT_DESCRIPTION, product.getDescribe());
        intent.putExtra(EXTRA_PRODUCT_INFORMATION, product.getInformation());
        intent.putExtra(EXTRA_PRODUCT_PRICE, product.getPrice());
        intent.putExtra(EXTRA_PRODUCT_FEE, product.getFee());
        intent.putExtra(EXTRA_PRODUCT_STATUS, product.getStatus());
        intent.putExtra(EXTRA_PRODUCT_PUBLIC_PRIVATE, product.getPublicPrivate());
        intent.putExtra(EXTRA_PRODUCT_PRIVATE_INFO, product.getPrivateInfo());
        intent.putExtra(EXTRA_PRODUCT_USER_ID, product.getUserId());
        intent.putExtra(EXTRA_PRODUCT_CREATED_AT, product.getCreatedAt().getTime());
        intent.putExtra(EXTRA_PRODUCT_VIEWS, product.getView());
        intent.putExtra(EXTRA_PRODUCT_USER_VIEW, product.getUserView());
        
        System.out.println("=== EditProductActivity: Intent created with userView: " + product.getUserView() + " ===");
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        session = new SessionManager(this);
        productViewModel = new ProductViewModel(getApplication());
        newImagePaths = new ArrayList<>();
        
        initViews();
        setupToolbar();
        loadProductData();
        setupListeners();
        setupImageRecyclerView();
    }

    private void initViews() {
        rvCurrentImages = findViewById(R.id.rvCurrentImages);
        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etInformation = findViewById(R.id.etInformation);
        etPrice = findViewById(R.id.etPrice);
        etPrivateInfo = findViewById(R.id.etPrivateInfo);
        etUserView = findViewById(R.id.etUserView);
        
        rgFee = findViewById(R.id.rgFee);
        rbSellerFee = findViewById(R.id.rbSellerFee);
        rbBuyerFee = findViewById(R.id.rbBuyerFee);
        
        rgPublicPrivate = findViewById(R.id.rgPublicPrivate);
        rbPublic = findViewById(R.id.rbPublic);
        rbPrivate = findViewById(R.id.rbPrivate);
        
        tilUserView = findViewById(R.id.tilUserView);
        btnAddImages = findViewById(R.id.btnAddImages);
        btnRemoveImages = findViewById(R.id.btnRemoveImages);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadProductData() {
        System.out.println("=== EditProductActivity: Loading product data from intent ===");
        
        // Get data from intent
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        String description = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);
        String information = getIntent().getStringExtra(EXTRA_PRODUCT_INFORMATION);
        Double price = getIntent().getDoubleExtra(EXTRA_PRODUCT_PRICE, 0.0);
        Double fee = getIntent().getDoubleExtra(EXTRA_PRODUCT_FEE, 0.0);
        String publicPrivate = getIntent().getStringExtra(EXTRA_PRODUCT_PUBLIC_PRIVATE);
        String privateInfo = getIntent().getStringExtra(EXTRA_PRODUCT_PRIVATE_INFO);
        String userView = getIntent().getStringExtra(EXTRA_PRODUCT_USER_VIEW);
        
        System.out.println("=== EditProductActivity: userView from intent: " + userView + " ===");
        System.out.println("=== EditProductActivity: publicPrivate from intent: " + publicPrivate + " ===");

        // Set form data
        etProductName.setText(productName);
        etDescription.setText(description);
        etInformation.setText(information);
        etPrice.setText(String.valueOf(price));
        
        // Decrypt private info if exists
        if (privateInfo != null && !privateInfo.trim().isEmpty()) {
            try {
                String decryptedInfo = PasswordUtils.decrypt(privateInfo);
                etPrivateInfo.setText(decryptedInfo);
            } catch (Exception e) {
                etPrivateInfo.setText(privateInfo);
            }
        }

        // Set fee selection
        if (fee == 1.0) {
            rbSellerFee.setChecked(true);
        } else if (fee == 2.0) {
            rbBuyerFee.setChecked(true);
        }

        // Set public/private selection
        if ("public".equals(publicPrivate)) {
            rbPublic.setChecked(true);
            tilUserView.setVisibility(View.GONE);
            System.out.println("=== EditProductActivity: Product is PUBLIC, hiding userView ===");
        } else {
            rbPrivate.setChecked(true);
            tilUserView.setVisibility(View.VISIBLE);
            etUserView.setText(userView);
            System.out.println("=== EditProductActivity: Product is PRIVATE, showing userView with value: " + userView + " ===");
        }

        // Create product object for later use
        product = new Product();
        product.setId(getIntent().getStringExtra(EXTRA_PRODUCT_ID));
        product.setUserId(getIntent().getIntExtra(EXTRA_PRODUCT_USER_ID, 0));
        product.setCreatedAt(new Date(getIntent().getLongExtra(EXTRA_PRODUCT_CREATED_AT, 0)));
        product.setView(getIntent().getIntExtra(EXTRA_PRODUCT_VIEWS, 0));
    }

    private void setupListeners() {
        btnAddImages.setOnClickListener(v -> pickImages());
        btnRemoveImages.setOnClickListener(v -> removeSelectedImages());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveChanges());

        rgPublicPrivate.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPrivate) {
                tilUserView.setVisibility(View.VISIBLE);
            } else {
                tilUserView.setVisibility(View.GONE);
                etUserView.setText("");
            }
        });
    }

    private void setupImageRecyclerView() {
        rvCurrentImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new ProductAdapter.ProductImagesAdapter();
        rvCurrentImages.setAdapter(imagesAdapter);
        
        // Load current images from database
        loadCurrentImagesFromDatabase();
    }

    private void loadCurrentImagesFromDatabase() {
        System.out.println("=== EditProductActivity: Loading current images from database ===");
        System.out.println("=== Product ID: " + (product != null ? product.getId() : "NULL") + " ===");
        
        if (product != null && product.getId() != null) {
            // Load images from database using ProductViewModel
            productViewModel.loadProductImages(product.getId(), new ProductViewModel.OnProductImagesLoadedListener() {
                @Override
                public void onImagesLoaded(List<ProductImages> images) {
                    currentImages = images != null ? images : new ArrayList<>();
                    System.out.println("=== EditProductActivity: Loaded " + currentImages.size() + " images from database ===");
                    imagesAdapter.updateImages(currentImages);
                }

                @Override
                public void onError(String error) {
                    System.out.println("=== EditProductActivity: Error loading images: " + error + " ===");
                    currentImages = new ArrayList<>();
                    imagesAdapter.updateImages(currentImages);
                }
            });
        } else {
            System.out.println("=== EditProductActivity: Product is null, cannot load images ===");
            currentImages = new ArrayList<>();
            imagesAdapter.updateImages(currentImages);
        }
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_IMAGE_PICK);
    }

    private void removeSelectedImages() {
        // TODO: Implement image removal logic
        Toast.makeText(this, "Tính năng xóa ảnh đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void saveChanges() {
        if (!validateForm()) {
            return;
        }

        // Get form data
        String productName = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String information = etInformation.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String privateInfo = etPrivateInfo.getText().toString().trim();
        String userView = etUserView.getText().toString().trim();

        // Parse price
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Giá không hợp lệ");
            return;
        }

        // Get fee selection
        double fee = rbSellerFee.isChecked() ? 1.0 : 2.0;

        // Get public/private selection
        String publicPrivate = rbPublic.isChecked() ? "public" : "private";

        // Encrypt private info
        String encryptedPrivateInfo = "";
        if (!privateInfo.isEmpty()) {
            try {
                encryptedPrivateInfo = PasswordUtils.encrypt(privateInfo);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mã hóa thông tin riêng tư", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Update product object
        product.setProductName(productName);
        product.setDescribe(description);
        product.setInformation(information);
        product.setPrice(price);
        product.setFee(fee);
        product.setPublicPrivate(publicPrivate);
        product.setPrivateInfo(encryptedPrivateInfo);
        product.setUserView(userView);
        product.setUpdatedAt(new Date());

        System.out.println("=== EditProductActivity: Calling ViewModel to update product ===");
        
        // Call ViewModel to update product in database
        productViewModel.updateProduct(product, newImagePaths, new ProductViewModel.OnProductUpdateListener() {
            @Override
            public void onSuccess() {
                System.out.println("=== EditProductActivity: Product updated successfully ===");
                Toast.makeText(EditProductActivity.this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_LONG).show();
                
                // Set result to indicate successful update
                Intent resultIntent = new Intent();
                resultIntent.putExtra("PRODUCT_UPDATED", true);
                setResult(RESULT_OK, resultIntent);
                
                finish();
            }

            @Override
            public void onError(String error) {
                System.out.println("=== EditProductActivity: Error updating product: " + error + " ===");
                Toast.makeText(EditProductActivity.this, "Lỗi cập nhật sản phẩm: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate product name
        String productName = etProductName.getText().toString().trim();
        if (productName.isEmpty()) {
            etProductName.setError("Tên sản phẩm không được để trống");
            isValid = false;
        }

        // Validate price
        String priceStr = etPrice.getText().toString().trim();
        if (priceStr.isEmpty()) {
            etPrice.setError("Giá không được để trống");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 10000) {
                    etPrice.setError("Giá phải lớn hơn 10.000 VNĐ");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Giá không hợp lệ");
                isValid = false;
            }
        }

        // Validate private info
        String privateInfo = etPrivateInfo.getText().toString().trim();
        if (privateInfo.isEmpty()) {
            etPrivateInfo.setError("Thông tin riêng tư không được để trống");
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple images selected
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    newImagePaths.add(imageUri.toString());
                }
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                newImagePaths.add(imageUri.toString());
            }
            
            Toast.makeText(this, "Đã chọn " + newImagePaths.size() + " ảnh", Toast.LENGTH_SHORT).show();
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
