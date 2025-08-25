package com.group5.safezone.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditAuctionActivity extends AppCompatActivity {

    private EditText etProductName, etDescription, etPrice, etStartPrice, etMinIncrement;
    private EditText etStartTime, etEndTime;
    private Button btnPickStart, btnPickEnd, btnUpdate, btnPickImages;
    private TextView tvImagesInfo;
    private LinearLayout llSelectedImages;
    private TextView tvNoImages;

    private Date startDate, endDate;
    private ArrayList<Uri> pickedUris = new ArrayList<>();
    private ArrayList<String> existingImagePaths = new ArrayList<>();
    private SessionManager sessionManager;
    
    // Data to edit
    private String productId;
    private Auctions auction;
    private Product product;
    private List<ProductImages> existingImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_auction);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("product_id");
        }

        if (productId == null) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        if (sessionManager.getUserId() <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupPickers();
        loadAuctionData();
        
        btnUpdate.setOnClickListener(v -> updateAuction());
        btnPickImages.setOnClickListener(v -> openImagePicker());
    }

    private void initViews() {
        // Setup toolbar
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etStartPrice = findViewById(R.id.etStartPrice);
        etMinIncrement = findViewById(R.id.etMinIncrement);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnPickStart = findViewById(R.id.btnPickStart);
        btnPickEnd = findViewById(R.id.btnPickEnd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnPickImages = findViewById(R.id.btnPickImages);
        tvImagesInfo = findViewById(R.id.tvImagesInfo);
        llSelectedImages = findViewById(R.id.llSelectedImages);
        tvNoImages = findViewById(R.id.tvNoImages);
    }

    private void setupPickers() {
        btnPickStart.setOnClickListener(v -> pickDateTime(true));
        btnPickEnd.setOnClickListener(v -> pickDateTime(false));
    }

    private void pickDateTime(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            TimePickerDialog tp = new TimePickerDialog(this, (timePicker, h, min) -> {
                Calendar c = Calendar.getInstance();
                c.set(y, m, d, h, min, 0);
                Date picked = c.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", new Locale("vi","VN"));
                if (isStart) {
                    startDate = picked;
                    etStartTime.setText(sdf.format(picked));
                } else {
                    endDate = picked;
                    etEndTime.setText(sdf.format(picked));
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            tp.show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void loadAuctionData() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Load auction and product data
                auction = db.auctionsDao().getAuctionByProductId(productId);
                product = db.productDao().getProductById(productId);
                existingImages = db.productImagesDao().getImagesByProductId(productId);
                
                if (auction == null || product == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy dữ liệu bài đấu giá", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Check if user owns this auction
                if (auction.getSellerUserId() != sessionManager.getUserId()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Bạn không có quyền sửa bài đấu giá này", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Check if auction is still pending
                if (!"pending".equals(auction.getStatus())) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Chỉ có thể sửa bài đấu giá đang chờ duyệt", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Load existing image paths
                existingImagePaths.clear();
                if (existingImages != null) {
                    for (ProductImages img : existingImages) {
                        existingImagePaths.add(img.getPath());
                    }
                }
                
                runOnUiThread(() -> {
                    populateFormData();
                    updateImagesDisplay();
                });
                
            } catch (Exception e) {
                Log.e("EditAuction", "Error loading auction data: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void populateFormData() {
        if (product != null) {
            etProductName.setText(product.getProductName());
            etDescription.setText(product.getDescribe());
            etPrice.setText(String.valueOf(product.getPrice()));
            etMinIncrement.setText(String.valueOf(product.getMinBidIncrement()));
        }
        
        if (auction != null) {
            etStartPrice.setText(String.valueOf(auction.getStartPrice()));
            
            if (auction.getStartTime() != null) {
                startDate = auction.getStartTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                etStartTime.setText(sdf.format(startDate));
            }
            
            if (auction.getEndTime() != null) {
                endDate = auction.getEndTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                etEndTime.setText(sdf.format(endDate));
            }
        }
    }

    private void updateAuction() {
        String name = etProductName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String startPriceStr = etStartPrice.getText().toString().trim();
        String minIncStr = etMinIncrement.getText().toString().trim();
        
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(startPriceStr) || startDate == null || endDate == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (existingImagePaths.isEmpty() && pickedUris.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double price = Double.parseDouble(priceStr);
        double startPrice = Double.parseDouble(startPriceStr);
        double minInc = TextUtils.isEmpty(minIncStr) ? 10000 : Double.parseDouble(minIncStr);
        
        if (!endDate.after(startDate)) {
            Toast.makeText(this, "Thời gian kết thúc phải sau thời gian bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận cập nhật")
            .setMessage("Bạn có chắc chắn muốn cập nhật bài đấu giá này? Bài đăng sẽ được gửi lại để admin duyệt.")
            .setPositiveButton("Cập nhật", (dialog, which) -> {
                performUpdate(name, desc, price, startPrice, minInc);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void performUpdate(String name, String desc, double price, double startPrice, double minInc) {
        // Show loading
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Đang cập nhật...");
        
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Update product
                product.setProductName(name);
                product.setDescribe(desc);
                product.setPrice(price);
                product.setMinBidIncrement(minInc);
                product.setIsAdminCheck(false); // Reset admin check status
                product.setStatus("pending");
                db.productDao().update(product);
                
                // Update auction
                auction.setStartPrice(startPrice);
                auction.setStartTime(startDate);
                auction.setEndTime(endDate);
                auction.setStatus("pending"); // Reset to pending
                db.auctionsDao().update(auction);
                
                // Handle images
                handleImageUpdates(db);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã cập nhật bài đấu giá, chờ admin duyệt", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                });
                
            } catch (Exception e) {
                Log.e("EditAuction", "Error updating auction: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Cập nhật");
                });
            }
        }).start();
    }

    private void handleImageUpdates(AppDatabase db) throws Exception {
        // Delete old images that are no longer needed
        if (existingImages != null) {
            for (ProductImages oldImg : existingImages) {
                boolean shouldKeep = false;
                for (String path : existingImagePaths) {
                    if (path.equals(oldImg.getPath())) {
                        shouldKeep = true;
                        break;
                    }
                }
                
                if (!shouldKeep) {
                    // Delete file and database record
                    File imageFile = new File(oldImg.getPath());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    db.productImagesDao().delete(oldImg);
                }
            }
        }
        
        // Add new images
        if (pickedUris != null && !pickedUris.isEmpty()) {
            for (int i = 0; i < pickedUris.size(); i++) {
                Uri imageUri = pickedUris.get(i);
                String imagePath = saveImageToInternalStorage(imageUri, "IMG_" + i + "_" + productId);
                
                if (imagePath != null) {
                    ProductImages img = new ProductImages();
                    img.setProductId(productId);
                    img.setName("IMG_" + i);
                    img.setPath(imagePath);
                    db.productImagesDao().insert(img);
                }
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri, String fileName) {
        try {
            // Get input stream from URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;
            
            // Decode bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;
            
            // Compress bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            
            // Create file in internal storage
            File imagesDir = new File(getFilesDir(), "product_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            File imageFile = new File(imagesDir, fileName + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(baos.toByteArray());
            fos.close();
            inputStream.close();
            
            return imageFile.getAbsolutePath();
            
        } catch (IOException e) {
            Log.e("EditAuction", "Error saving image: " + e.getMessage(), e);
            return null;
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh sản phẩm"), 9099);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9099 && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                // Single image selected
                pickedUris.add(data.getData());
            } else if (data.getClipData() != null) {
                // Multiple images selected
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    pickedUris.add(data.getClipData().getItemAt(i).getUri());
                }
            }
            
            updateImagesDisplay();
            Toast.makeText(this, "Đã chọn " + pickedUris.size() + " ảnh mới", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImagesDisplay() {
        int totalImages = existingImagePaths.size() + pickedUris.size();
        
        if (totalImages == 0) {
            tvImagesInfo.setText("Chưa có ảnh nào");
            tvNoImages.setVisibility(View.VISIBLE);
            llSelectedImages.setVisibility(View.GONE);
        } else {
            tvImagesInfo.setText("Tổng cộng " + totalImages + " ảnh");
            tvNoImages.setVisibility(View.GONE);
            llSelectedImages.setVisibility(View.VISIBLE);
            
            // Clear existing image views
            llSelectedImages.removeAllViews();
            
            // Show existing images
            for (String imagePath : existingImagePaths) {
                addImageView(imagePath, true);
            }
            
            // Show new picked images
            for (Uri uri : pickedUris) {
                addImageView(uri, false);
            }
        }
    }

    private void addImageView(Object imageSource, boolean isExisting) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(4, 4, 4, 4);
        
        // Add delete button for existing images
        if (isExisting) {
            imageView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("Xóa ảnh")
                    .setMessage("Bạn có muốn xóa ảnh này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        existingImagePaths.remove(imageSource);
                        updateImagesDisplay();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
                return true;
            });
        }
        
        // Load image
        try {
            if (isExisting) {
                String imagePath = (String) imageSource;
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.ic_image_placeholder);
                }
            } else {
                Uri uri = (Uri) imageSource;
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e("EditAuction", "Error loading image: " + e.getMessage(), e);
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
        
        llSelectedImages.addView(imageView);
    }
}
